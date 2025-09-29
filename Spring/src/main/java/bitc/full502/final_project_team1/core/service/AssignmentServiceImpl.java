package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.api.web.dto.AssignApproverRequestDTO;
import bitc.full502.final_project_team1.api.web.dto.AssignApproverResponseDTO;
import bitc.full502.final_project_team1.core.domain.entity.ApprovalEntity;
import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import bitc.full502.final_project_team1.core.domain.repository.ApprovalRepository;
import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
import bitc.full502.final_project_team1.core.domain.repository.UserBuildingAssignmentRepository;
import bitc.full502.final_project_team1.core.service.AssignmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final UserAccountRepository userRepo;
    private final BuildingRepository buildingRepo;
    private final UserBuildingAssignmentRepository assignmentRepo;
    private final ApprovalRepository approvalRepo;

    // 팀 규칙 예: 1=배정, 2=결재대기, 3=완료, 4=반려
    private static final int STATUS_ASSIGNED = 1;
    private static final int STATUS_WAITING_APPROVAL = 2;

    // -------------- 유틸 --------------
    private static boolean containsIgnoreCase(String s, String q) {
        return s != null && q != null && s.toLowerCase().contains(q.toLowerCase());
    }

    // -------------- 인터페이스 구현 --------------

    @Override
    @Transactional
    public List<Map<String, Object>> getAssignments(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");

        // Repository에 전용 메서드가 없다면 전수 조회 후 필터(데이터가 적은 환경 가정)
        List<UserBuildingAssignmentEntity> all = assignmentRepo.findAll();
        return all.stream()
                .filter(uba -> {
                    Long uid = (uba.getUser() != null) ? uba.getUser().getUserId() : uba.getId();
                    return Objects.equals(uid, userId);
                })
                .map(uba -> {
                    Long bId = uba.getBuildingId();
                    String lotAddr = buildingRepo.findById(bId)
                            .map(BuildingEntity::getLotAddress)
                            .orElse(null);
                    return Map.<String, Object>of(
                            "buildingId", bId,
                            "lotAddress", lotAddr
                    );
                })
                .collect(Collectors.toList());
    }

    //     유저별 배정 목록을 (buildingId, lotAddress) 맵으로 반환
//    @Transactional(readOnly = true)
//    public List<Map<String, Object>> getAssignments(Long userId) {
//        List<Object[]> rows = assignmentRepo.findPairsByUserId(userId);
//        List<Map<String, Object>> out = new ArrayList<>(rows.size());
//        for (Object[] r : rows) {
//            Map<String, Object> m = new HashMap<>();
//            m.put("buildingId", (Long) r[0]);
//            m.put("lotAddress", (String) r[1]);
//            out.add(m);
//        }
//        return out;
//    }

    @Override
    @Transactional
    public int assignToUser(Long userId, List<Long> buildingIds) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (buildingIds == null || buildingIds.isEmpty()) return 0;

        UserAccountEntity user = getUserOrThrow(userId);

        int count = 0;
        for (Long buildingId : new LinkedHashSet<>(buildingIds)) {
            // upsert: user_building_assignment
            UserBuildingAssignmentEntity uba = assignmentRepo.findByBuildingId(buildingId)
                    .orElseGet(() -> UserBuildingAssignmentEntity.builder()
                            .buildingId(buildingId)
                            .build());

            // 조사원 배정
            uba.setUser(user);
            uba.setStatus(STATUS_ASSIGNED);
            // approval_id는 조사원 배정 단계에서는 null 유지
            assignmentRepo.save(uba);

            // Building.assignedUser/상태 동기화
            buildingRepo.findById(buildingId).ifPresent(b -> {
                b.setAssignedUser(user); // FK(assigned_user_id) 자동 갱신
                b.setStatus(STATUS_ASSIGNED);
                buildingRepo.save(b);
            });

            count++;
        }
        return count;
    }

    @Override
    public UserAccountEntity getUserOrThrow(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    @Override
    public List<UserAccountEntity> searchApprovers(String keyword) {
        String k = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return userRepo.findApprovers(k);   // ✅ 이제 APPROVER만 반환
    }

    @Override
    @Transactional
    public AssignApproverResponseDTO assignApprover(AssignApproverRequestDTO req) {
        var approver = getUserOrThrow(req.getUserId());

        var buildingIds = new java.util.LinkedHashSet<>(
                req.getBuildingIds() == null ? java.util.List.of() : req.getBuildingIds());

        var updatedIds = new java.util.ArrayList<Long>();
        var alreadyAssigned = new java.util.ArrayList<Long>();
        var noResearcher = new java.util.ArrayList<Long>();
        var notFound = new java.util.ArrayList<Long>();

        for (Long buildingId : buildingIds) {
            var ubaOpt = assignmentRepo.findByBuildingId(buildingId);
            if (ubaOpt.isEmpty()) {
                notFound.add(buildingId);
                continue;
            }
            var uba = ubaOpt.get();

            // 조사원 확인
            Long surveyorId = (uba.getUser() != null) ? uba.getUser().getUserId() : uba.getId();
            if (surveyorId == null) {
                noResearcher.add(buildingId);
                continue;
            }

            // 이미 approval_id 있으면 skip
            if (uba.getApprovalId() != null) {
                alreadyAssigned.add(buildingId);
                continue;
            }

            var building = buildingRepo.findById(buildingId)
                    .orElse(null);
            if (building == null) {
                notFound.add(buildingId);
                continue;
            }

            // (선택) 대기중 레코드 재사용, 없으면 생성
            var approval = approvalRepo.findPendingByBuildingId(buildingId)
                    .orElseGet(() -> {
                        var surveyor = userRepo.findById(surveyorId).orElse(null);
                        var a = ApprovalEntity.builder()
                                .approver(approver)   // 결재자
                                .building(building)   // 건물
                                .surveyor(surveyor)   // 조사원
                                .approvedAt(null)     // 대기 상태
                                .rejectReason(null)
                                .build();
                        return approvalRepo.save(a);
                    });

            // 여기! approval PK를 UBA.approval_id에 세팅
            uba.setApprovalId(approval.getId());

            // 상태 올리기: 결재 대기
            if (uba.getStatus() == null || uba.getStatus() < STATUS_WAITING_APPROVAL) {
                uba.setStatus(STATUS_WAITING_APPROVAL);
            }
            // 영속 상태라 커밋 시 업데이트됨
            updatedIds.add(buildingId);
        }

        return AssignApproverResponseDTO.builder()
                .success(true)
                .assignedCount(updatedIds.size())
                .updatedIds(updatedIds)
                .alreadyAssigned(alreadyAssigned)
                .noResearcher(noResearcher)
                .notFound(notFound)
                .build();
    }

    @Override
    @Transactional
    public void rejectAssignment(Long userId, Long buildingId) {
        // 1) 본인 배정건 검증 후 삭제 (FK 기준으로 정확히)
        var asg = assignmentRepo
                .findByBuildingIdAndUser_UserId(buildingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("배정 내역이 없습니다."));
        assignmentRepo.delete(asg);
        // 필요시: assignmentRepo.deleteByBuildingIdAndUser_UserId(buildingId, userId);

        // 2) 건물 상태/배정자 초기화
        BuildingEntity b = buildingRepo.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 건물이 없습니다."));
        b.setStatus(0);            // 미배정
        b.setAssignedUser(null);   // 배정자 해제

        // 3) 명시 저장(더티체킹이 환경에 따라 안 먹는 경우 대비)
        buildingRepo.save(b);
        // 선택: 강제 플러시 (디버깅 중엔 확실히 반영)
        // buildingRepo.flush();
    }







}


//package bitc.full502.final_project_team1.core.service;
//
//import bitc.full502.final_project_team1.api.web.dto.AssignApproverRequestDTO;
//import bitc.full502.final_project_team1.api.web.dto.AssignApproverResponseDTO;
//import bitc.full502.final_project_team1.core.domain.entity.ApprovalEntity;
//import bitc.full502.final_project_team1.core.domain.entity.BuildingEntity;
//import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
//import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
//import bitc.full502.final_project_team1.core.domain.repository.ApprovalRepository;
//import bitc.full502.final_project_team1.core.domain.repository.BuildingRepository;
//import bitc.full502.final_project_team1.core.domain.repository.UserAccountRepository;
//import bitc.full502.final_project_team1.core.domain.repository.UserBuildingAssignmentRepository;
//import bitc.full502.final_project_team1.core.service.AssignmentService;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class AssignmentServiceImpl implements AssignmentService {
//
//    private final UserAccountRepository userRepo;
//    private final BuildingRepository buildingRepo;
//    private final UserBuildingAssignmentRepository assignmentRepo;
//    private final ApprovalRepository approvalRepo;
//
//    // 팀 규칙 예: 1=배정, 2=결재대기, 3=완료, 4=반려
//    private static final int STATUS_ASSIGNED = 1;
//    private static final int STATUS_WAITING_APPROVAL = 2;
//
//    // -------------- 유틸 --------------
//    private static boolean containsIgnoreCase(String s, String q) {
//        return s != null && q != null && s.toLowerCase().contains(q.toLowerCase());
//    }
//
//    // -------------- 인터페이스 구현 --------------
//
//    @Override
//    @Transactional
//    public List<Map<String, Object>> getAssignments(Long userId) {
//        if (userId == null) throw new IllegalArgumentException("userId is required");
//
//        // Repository에 전용 메서드가 없다면 전수 조회 후 필터(데이터가 적은 환경 가정)
//        List<UserBuildingAssignmentEntity> all = assignmentRepo.findAll();
//        return all.stream()
//            .filter(uba -> {
//                Long uid = (uba.getUser() != null) ? uba.getUser().getUserId() : uba.getId();
//                return Objects.equals(uid, userId);
//            })
//            .map(uba -> {
//                Long bId = uba.getBuildingId();
//                String lotAddr = buildingRepo.findById(bId)
//                    .map(BuildingEntity::getLotAddress)
//                    .orElse(null);
//                return Map.<String, Object>of(
//                    "buildingId", bId,
//                    "lotAddress", lotAddr
//                );
//            })
//            .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional
//    public int assignToUser(Long userId, List<Long> buildingIds) {
//        if (userId == null) throw new IllegalArgumentException("userId is required");
//        if (buildingIds == null || buildingIds.isEmpty()) return 0;
//
//        UserAccountEntity user = getUserOrThrow(userId);
//
//        int count = 0;
//        for (Long buildingId : new LinkedHashSet<>(buildingIds)) {
//            // upsert: user_building_assignment
//            UserBuildingAssignmentEntity uba = assignmentRepo.findByBuildingId(buildingId)
//                .orElseGet(() -> UserBuildingAssignmentEntity.builder()
//                    .buildingId(buildingId)
//                    .build());
//
//            // 조사원 배정
//            uba.setUser(user);
//            uba.setStatus(STATUS_ASSIGNED);
//            // approval_id는 조사원 배정 단계에서는 null 유지
//            assignmentRepo.save(uba);
//
//            // Building.assignedUser/상태 동기화
//            buildingRepo.findById(buildingId).ifPresent(b -> {
//                b.setAssignedUser(user); // FK(assigned_user_id) 자동 갱신
//                b.setStatus(STATUS_ASSIGNED);
//                buildingRepo.save(b);
//            });
//
//            count++;
//        }
//        return count;
//    }
//
//    @Override
//    public UserAccountEntity getUserOrThrow(Long userId) {
//        return userRepo.findById(userId)
//            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
//    }
//
//    @Override
//    public List<UserAccountEntity> searchApprovers(String keyword) {
//        String k = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
//        return userRepo.findApprovers(k);   // ✅ 이제 APPROVER만 반환
//    }
//
//    @Override
//    @Transactional
//    public AssignApproverResponseDTO assignApprover(AssignApproverRequestDTO req) {
//        var approver = getUserOrThrow(req.getUserId());
//
//        var buildingIds = new java.util.LinkedHashSet<>(
//            req.getBuildingIds() == null ? java.util.List.of() : req.getBuildingIds());
//
//        var updatedIds = new java.util.ArrayList<Long>();
//        var alreadyAssigned = new java.util.ArrayList<Long>();
//        var noResearcher   = new java.util.ArrayList<Long>();
//        var notFound       = new java.util.ArrayList<Long>();
//
//        for (Long buildingId : buildingIds) {
//            var ubaOpt = assignmentRepo.findByBuildingId(buildingId);
//            if (ubaOpt.isEmpty()) { notFound.add(buildingId); continue; }
//            var uba = ubaOpt.get();
//
//            // 조사원 확인
//            Long surveyorId = (uba.getUser() != null) ? uba.getUser().getUserId() : uba.getId();
//            if (surveyorId == null) { noResearcher.add(buildingId); continue; }
//
//            // 이미 approval_id 있으면 skip
//            if (uba.getApprovalId() != null) { alreadyAssigned.add(buildingId); continue; }
//
//            var building = buildingRepo.findById(buildingId)
//                .orElse(null);
//            if (building == null) { notFound.add(buildingId); continue; }
//
//            // (선택) 대기중 레코드 재사용, 없으면 생성
//            var approval = approvalRepo.findPendingByBuildingId(buildingId)
//                .orElseGet(() -> {
//                    var surveyor = userRepo.findById(surveyorId).orElse(null);
//                    var a = ApprovalEntity.builder()
//                        .approver(approver)   // 결재자
//                        .building(building)   // 건물
//                        .surveyor(surveyor)   // 조사원
//                        .approvedAt(null)     // 대기 상태
//                        .rejectReason(null)
//                        .build();
//                    return approvalRepo.save(a);
//                });
//
//            // 여기! approval PK를 UBA.approval_id에 세팅
//            uba.setApprovalId(approval.getId());
//
//            // 상태 올리기: 결재 대기
//            if (uba.getStatus() == null || uba.getStatus() < STATUS_WAITING_APPROVAL) {
//                uba.setStatus(STATUS_WAITING_APPROVAL);
//            }
//            // 영속 상태라 커밋 시 업데이트됨
//            updatedIds.add(buildingId);
//        }
//
//        return AssignApproverResponseDTO.builder()
//            .success(true)
//            .assignedCount(updatedIds.size())
//            .updatedIds(updatedIds)
//            .alreadyAssigned(alreadyAssigned)
//            .noResearcher(noResearcher)
//            .notFound(notFound)
//            .build();
//    }
//}

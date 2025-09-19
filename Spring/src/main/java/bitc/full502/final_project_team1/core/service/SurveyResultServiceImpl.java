package bitc.full502.final_project_team1.core.service;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import bitc.full502.final_project_team1.core.domain.repository.SurveyResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyResultServiceImpl implements SurveyResultService {

    private final SurveyResultRepository repo;

    @Override
    public Page<SurveyResultEntity> search(String status, String rawKw, Pageable pageable) {
        String normStatus = (status == null || status.isBlank()) ? null : status.trim();

        // 유니코드 공백 제거 후 빈값이면 null
        String normKw = null;
        if (rawKw != null) {
            String noWs = rawKw.replaceAll("\\s+", ""); // 모든 공백 제거
            if (!noWs.isEmpty()) {
                normKw = rawKw.trim();
            }
        }

        if (normKw == null && normStatus == null) {
            return repo.findAll(pageable); // 완전 무필터
        }
        return repo.search(normStatus, normKw, pageable); // 상태만/상태+키워드
    }


    @Override
    public SurveyResultEntity findByIdOrThrow(Long id) {
        return repo.findById(id).orElseThrow();
    }

    @Override
    @Transactional // 쓰기 트랜잭션
    public int approveBulk(List<Long> ids) {
        var list = repo.findAllById(ids);
        int count = 0;
        for (var e : list) {
            if (!"APPROVED".equalsIgnoreCase(e.getStatus())) {
                e.setStatus("APPROVED");
                count++;
            }
        }
        return count;
    }

    @Override
    @Transactional // 쓰기 트랜잭션
    public int rejectBulk(List<Long> ids) {
        var list = repo.findAllById(ids);
        int count = 0;
        for (var e : list) {
            if (!"REJECTED".equalsIgnoreCase(e.getStatus())) {
                e.setStatus("REJECTED");
                count++;
            }
        }
        return count;
    }

    @Override
    public Page<SurveyResultEntity> pageSample(int size) {
        return repo.findAll(
                PageRequest.of(0, Math.max(1, size), Sort.by(Sort.Direction.DESC, "id"))
        );
    }
}

package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.UserBuildingAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBuildingAssignmentRepository extends JpaRepository<UserBuildingAssignmentEntity, Long> {

    /** 유저의 배정 목록 (building_id, lot_address) DTO 프로젝션 없이 Map/DTO는 서비스/컨트롤러에서 조립 */
    @Query("""
           select a.buildingId, b.lotAddress
           from UserBuildingAssignmentEntity a
           join a.building b
           where a.user.userId = :userId
           order by a.buildingId
           """)
    List<Object[]> findPairsByUserId(Integer userId);

    /** 특정 지역(키워드) 배정만 삭제 (라운드로빈 재배정 전에 사용) */
    @Modifying
    @Query("""
           delete from UserBuildingAssignmentEntity a
           where a.buildingId in (
                select b.id from BuildingEntity b
                where b.lotAddress like %:keyword%
           )
           """)
    int deleteAllByLotAddressLike(String keyword);

    @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u")
    long countAllAssignments();

    @Query("SELECT COUNT(u) FROM UserBuildingAssignmentEntity u WHERE u.status = :status")
    long countByStatus(@Param("status") int status);

}
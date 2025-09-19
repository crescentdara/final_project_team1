package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyResultRepository extends JpaRepository<SurveyResultEntity, Long> {

    List<SurveyResultEntity> findByUser_UserIdAndStatus(Integer userId, String status);

    long countByUser_UserIdAndStatus(Integer userId, String status);

    interface StatusCount {
        String getStatus();
        long getCnt();
    }

    @Query("""
        select s.status as status, count(s) as cnt
        from SurveyResultEntity s
        where s.user.userId = :userId
        group by s.status
        """)
    List<StatusCount> countGroupByStatus(@Param("userId") Integer userId);

    @Query("""
        select s
        from SurveyResultEntity s
        where s.user.userId = :userId
          and (:status is null or s.status = :status)
        order by case when s.updatedAt is null then 1 else 0 end,
                 s.updatedAt desc,
                 s.createdAt desc
        """)
    Page<SurveyResultEntity> findByUserAndStatusPage(
            @Param("userId") Integer userId,
            @Param("status") String status,
            Pageable pageable
    );



}


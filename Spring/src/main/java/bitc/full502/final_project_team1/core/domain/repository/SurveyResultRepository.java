package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.Optional;


import java.util.List;

@Repository
public interface SurveyResultRepository extends JpaRepository<SurveyResultEntity, Long> {

    List<SurveyResultEntity> findByUser_UserIdAndStatus(Long userId, String status);

    Long countByUser_UserIdAndStatus(Long userId, String status);
    // long countByUser_UserIdAndStatus(Long userId, String status);

    interface StatusCount {
        String getStatus();
        Long getCnt();
    }

    @Query("""
        select s.status as status, count(s) as cnt
        from SurveyResultEntity s
        where s.user.userId = :userId
        group by s.status
        """)
    List<StatusCount> countGroupByStatus(@Param("userId") Long userId);

    @Query("""
    select s
    from SurveyResultEntity s
    where s.user.userId = :userId
      and (:status is null or upper(s.status) = upper(:status))
    order by case when s.updatedAt is null then 1 else 0 end,
             s.updatedAt desc,
             s.createdAt desc
    """)
    Page<SurveyResultEntity> findByUserAndStatusPage(
            @Param("userId") Long userId,
            @Param("status") String status,
            Pageable pageable
    );


    /** 무필터 전체 로딩에서도 user, building 같이 가져오기 (N+1 방지) */
    @Override
    @EntityGraph(attributePaths = {"user", "building"})
    Page<SurveyResultEntity> findAll(Pageable pageable);

    // 결재 대기 중 검색
    @EntityGraph(attributePaths = {"user", "building"})
    @Query("""
        select sr from SurveyResultEntity sr
        left join sr.user u
        left join sr.building b
        where (:status is null or upper(sr.status) = upper(:status))
        and (
          :kw is null or LENGTH(TRIM(:kw)) = 0 or
          lower(concat('m-', sr.id)) like lower(concat('%', TRIM(:kw), '%')) or
          (u is not null and lower(coalesce(u.name, u.username)) like lower(concat('%', TRIM(:kw), '%'))) or
          (b is not null and lower(b.lotAddress) like lower(concat('%', TRIM(:kw), '%')))
        )
    """)
    Page<SurveyResultEntity> search(@Param("status") String status,
                                    @Param("kw") String keyword,
                                    Pageable pageable);

    /** 단건 조회 시 user, building 반드시 fetch */
    @EntityGraph(attributePaths = {"user", "building"})
    @Query("select sr from SurveyResultEntity sr where sr.id = :id")
    Optional<SurveyResultEntity> findByIdWithUserAndBuilding(@Param("id") Long id);

    Optional<SurveyResultEntity>
    findTopByUser_UserIdAndBuilding_IdOrderByUpdatedAtDescCreatedAtDesc(Long userId, Long buildingId);

    Optional<SurveyResultEntity> findByUser_UserIdAndBuilding_IdAndStatus(Long userId, Long buildingId, String status);

    // 금일 완료
    @Query("""
select count(s)
from SurveyResultEntity s
where s.user.userId = :userId
  and s.status in ('SENT', 'APPROVED')
  and s.createdAt >= :todayStart
""")


    long countSentToday(@Param("userId") Long userId, @Param("todayStart") LocalDateTime todayStart);
    // 최신 1건 가져오기
    Optional<SurveyResultEntity> findTopByBuilding_IdOrderByIdDesc(Long buildingId);

    // 전체 survey_result 에서 status 값별 건수 카운트
    long countByStatus(String status);


    // 조사자 삭제 - 특정 건물의 최신 조사 결과 조회
    SurveyResultEntity findTop1ByBuilding_IdOrderByCreatedAtDesc(Long buildingId);

    // 조사자 삭제 - 특정 건물 전체 결과 삭제 (TEMP/REJECTED 정리용)
    void deleteByBuilding_Id(Long buildingId);

}



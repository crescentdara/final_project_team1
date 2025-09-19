package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SurveyResultRepository extends JpaRepository<SurveyResultEntity, Long> {

    /** 무필터 전체 로딩에서도 user, building 같이 가져오기 (N+1 방지) */
    @Override
    @EntityGraph(attributePaths = {"user", "building"})
    Page<SurveyResultEntity> findAll(Pageable pageable);

    /** 검색 시에도 user, building fetch */
    @EntityGraph(attributePaths = {"user", "building"})
    @Query("""
        select sr from SurveyResultEntity sr
        where (:status is null or upper(sr.status) = upper(:status))
          and (
              :kw is null or :kw = '' 
              or lower(concat('m-', sr.id)) like lower(concat('%', :kw, '%')) 
              or (sr.user is not null and lower(coalesce(sr.user.name, sr.user.username)) like lower(concat('%', :kw, '%'))) 
              or (sr.building is not null and lower(sr.building.lotAddress) like lower(concat('%', :kw, '%')))
          )
    """)
    Page<SurveyResultEntity> search(@Param("status") String status,
                                    @Param("kw") String keyword,
                                    Pageable pageable);

    /** 단건 조회 시 user, building 반드시 fetch */
    @EntityGraph(attributePaths = {"user", "building"})
    @Query("select sr from SurveyResultEntity sr where sr.id = :id")
    Optional<SurveyResultEntity> findByIdWithUserAndBuilding(@Param("id") Long id);
}

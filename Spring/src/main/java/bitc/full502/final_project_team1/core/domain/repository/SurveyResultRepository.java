package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.SurveyResultEntity;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SurveyResultRepository extends JpaRepository<SurveyResultEntity, Long> {

    /** 무필터 전체 로딩에서도 user, building을 같이 가져와 N+1 방지 */
    @Override
    @EntityGraph(attributePaths = {"user", "building"})
    Page<SurveyResultEntity> findAll(Pageable pageable);


     @EntityGraph(attributePaths = {"user", "building"})
     @Query("""
     select sr from SurveyResultEntity sr
     left join sr.user u
     left join sr.building b
     where (:status is null or upper(sr.status) = upper(:status))
     and (
     :kw is null or :kw = '' or
     lower(concat('m-', sr.id)) like lower(concat('%', :kw, '%')) or
     (u is not null and lower(coalesce(u.name, u.username)) like lower(concat('%', :kw, '%'))) or
     (b is not null and lower(b.lotAddress) like lower(concat('%', :kw, '%')))
     )
     """)
     Page<SurveyResultEntity> search(@Param("status") String status,
     @Param("kw") String keyword,
     Pageable pageable);

}

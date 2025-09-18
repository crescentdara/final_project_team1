package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.SurveyApprovalEntity;
import bitc.full502.final_project_team1.core.domain.entity.SurveyApprovalEntity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SurveyApprovalRepository extends JpaRepository<SurveyApprovalEntity, Long> {
    // 그대로 OK (kw가 빈 문자열이면 '%%'가 되므로 컨트롤러에서 막습니다)
    @Query("""
  select s from SurveyApprovalEntity s
  where (:status is null or s.status = :status)
    and (
      lower(s.caseNo)       like lower(concat('%', :kw, '%')) or
      lower(s.investigator) like lower(concat('%', :kw, '%')) or
      lower(s.address)      like lower(concat('%', :kw, '%')) or
      lower(str(s.priority)) like lower(concat('%', :kw, '%')) or
      lower(str(s.status))   like lower(concat('%', :kw, '%'))
    )
""")
    Page<SurveyApprovalEntity> search(Status status, String kw, Pageable pageable);

}

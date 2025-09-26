// src/main/java/bitc/full502/final_project_team1/core/domain/repository/UserAccountRepository.java
package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import bitc.full502.final_project_team1.core.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Long> {

    Optional<UserAccountEntity> findByUsernameAndStatus(String username, Integer status);

    List<UserAccountEntity> findTop100ByNameContainingOrUsernameContainingOrderByUserId(
        String nameKeyword, String usernameKeyword
    );

    // 🔍 role=RESEARCHER 전체 조회
    List<UserAccountEntity> findByRole(Role role);

    // 🔍 role=RESEARCHER + 이름/username 검색
    List<UserAccountEntity> findByRoleAndNameContainingOrRoleAndUsernameContaining(
            Role role1, String name,
            Role role2, String username
    );

    List<UserAccountEntity> findTop200ByOrderByUserIdAsc();

    // (기존) 이름/아이디 contains ignore case
    List<UserAccountEntity>
    findTop200ByNameContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrderByUserIdAsc(
        String nameKeyword, String usernameKeyword);

    /* ---------- 부분일치 + 대소문자 무시 쿼리들 ---------- */

    /** 전체 필드(any): userId(문자열 비교), username, name, role */
    @Query("select u from UserAccountEntity u " +
        "where str(u.userId) like concat('%', :kw, '%') " +
        "   or lower(u.username) like lower(concat('%', :kw, '%')) " +
        "   or lower(u.name)     like lower(concat('%', :kw, '%')) " +
        "   or lower(str(u.role)) like lower(concat('%', :kw, '%'))")
    List<UserAccountEntity> searchAllLikeIgnoreCase(@Param("kw") String kw, Pageable pageable);

    /** ID 부분일치 (숫자를 문자열로 변환해서 비교) */
    @Query("select u from UserAccountEntity u " +
        "where str(u.userId) like concat('%', :kw, '%')")
    List<UserAccountEntity> searchByIdLike(@Param("kw") String kw, Pageable pageable);

    /** username 부분일치 (대소문자 무시) */
    @Query("select u from UserAccountEntity u " +
        "where lower(u.username) like lower(concat('%', :kw, '%'))")
    List<UserAccountEntity> searchByUsernameLikeIgnoreCase(@Param("kw") String kw, Pageable pageable);

    /** name 부분일치 (대소문자 무시) */
    @Query("select u from UserAccountEntity u " +
        "where lower(u.name) like lower(concat('%', :kw, '%'))")
    List<UserAccountEntity> searchByNameLikeIgnoreCase(@Param("kw") String kw, Pageable pageable);

    /** role 부분일치 (대소문자 무시, enum을 문자열로 비교) */
    @Query("select u from UserAccountEntity u " +
        "where lower(str(u.role)) like lower(concat('%', :kw, '%'))")
    List<UserAccountEntity> searchByRoleLikeIgnoreCase(@Param("kw") String kw, Pageable pageable);

    // 조사자 상세 정보
    List<UserAccountEntity> findAllByRoleOrderByUserIdAsc(Role role);

    // 페이징 - 개별 검색
    Page<UserAccountEntity> findByRole(Role role, Pageable pageable);

    Page<UserAccountEntity> findByRoleAndNameContainingIgnoreCase(
            Role role, String name, Pageable pageable);

    Page<UserAccountEntity> findByRoleAndUsernameContainingIgnoreCase(
            Role role, String username, Pageable pageable);

    Page<UserAccountEntity> findByRoleAndEmpNoContainingIgnoreCase(
            Role role, String empNo, Pageable pageable);

    // 전체 검색 (이름 + 아이디 + 사번)
    @Query("SELECT u FROM UserAccountEntity u " +
            "WHERE u.role = :role " +
            "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "OR LOWER(u.empNo) LIKE LOWER(CONCAT('%', :kw, '%')))")
    Page<UserAccountEntity> searchAllFields(@Param("role") Role role,
                                            @Param("kw") String keyword,
                                            Pageable pageable);

    // 중복 확인
    boolean existsByUsername(String username);

    // 🔍 role=RESEARCHER + preferredRegion 부분일치
    @Query("SELECT u FROM UserAccountEntity u " +
            "WHERE u.role = :role " +
            "AND u.preferredRegion LIKE CONCAT('%', :region, '%')")
    List<UserAccountEntity> findByRoleAndPreferredRegionLike(
            @Param("role") Role role,
            @Param("region") String region
    );


    // 🔍 role=RESEARCHER + preferredRegion + keyword (부분일치)
    @Query("SELECT u FROM UserAccountEntity u " +
            "WHERE u.role = :role " +
            "AND u.preferredRegion LIKE CONCAT('%', :region, '%') " +
            "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "     OR LOWER(u.username) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "     OR LOWER(u.empNo) LIKE LOWER(CONCAT('%', :kw, '%')))")
    List<UserAccountEntity> findByRoleAndPreferredRegionAndKeyword(
            @Param("role") Role role,
            @Param("region") String region,
            @Param("kw") String keyword
    );

    // 🔍 role=RESEARCHER + 이름/username 검색 (부분일치)
    @Query("SELECT u FROM UserAccountEntity u " +
            "WHERE u.role = :role " +
            "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "     OR LOWER(u.username) LIKE LOWER(CONCAT('%', :kw, '%')))")
    List<UserAccountEntity> findByRoleAndKeyword(
            @Param("role") Role role,
            @Param("kw") String keyword
    );


    // UserAccountRepository
    Optional<UserAccountEntity> findFirstByRole(String role);


    @Query("""
    select u from UserAccountEntity u
     where
       (:role is null or upper(u.role) = upper(:role))
       and (
            :kw is null or :kw = '' or
            lower(coalesce(u.name, ''))     like lower(concat('%', :kw, '%')) or
            lower(coalesce(u.username, '')) like lower(concat('%', :kw, '%')) or
            lower(coalesce(u.empNo, ''))    like lower(concat('%', :kw, '%')) or
            str(u.userId) like concat('%', :kw, '%')
       )
     order by coalesce(u.name, u.username) asc
    """)
    List<UserAccountEntity> searchApprovers(@Param("role") String role,
                                            @Param("kw") String keyword);

    // "APPROVER 만 찾는 쿼리"
    @Query("""
  select u
    from UserAccountEntity u
   where u.role = bitc.full502.final_project_team1.core.domain.enums.Role.APPROVER
     and (:kw is null
          or lower(u.username) like lower(concat('%', :kw, '%'))
          or lower(u.name)     like lower(concat('%', :kw, '%'))
          or lower(u.empNo)    like lower(concat('%', :kw, '%')))
""")
    List<UserAccountEntity> findApprovers(@Param("kw") String kw);
}

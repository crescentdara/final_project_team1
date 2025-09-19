// src/main/java/bitc/full502/final_project_team1/core/domain/repository/UserAccountRepository.java
package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Integer> {
    List<UserAccountEntity> findTop100ByNameContainingOrUsernameContainingOrderByUserId(
            String nameKeyword, String usernameKeyword
    );

    // 🔍 role=EDITOR 전체 조회
    List<UserAccountEntity> findByRole(UserAccountEntity.Role role);

    // 🔍 role=EDITOR + 이름/username 검색
    List<UserAccountEntity> findByRoleAndNameContainingOrRoleAndUsernameContaining(
            UserAccountEntity.Role role1, String name,
            UserAccountEntity.Role role2, String username
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
}

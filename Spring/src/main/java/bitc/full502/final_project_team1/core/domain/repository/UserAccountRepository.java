package bitc.full502.final_project_team1.core.domain.repository;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Integer> {

    // 🔍 role=EDITOR 전체 조회
    List<UserAccountEntity> findByRole(UserAccountEntity.Role role);

    // 🔍 role=EDITOR + 이름/username 검색
    List<UserAccountEntity> findByRoleAndNameContainingOrRoleAndUsernameContaining(
            UserAccountEntity.Role role1, String name,
            UserAccountEntity.Role role2, String username
    );
}

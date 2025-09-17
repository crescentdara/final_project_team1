package bitc.full502.final_project_team1.core.domain.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Integer> {
    
    Optional<UserAccountEntity> findByUsernameAndStatus(String username, Integer status);

    List<UserAccountEntity> findTop100ByNameContainingOrUsernameContainingOrderByUserId(
            String nameKeyword, String usernameKeyword
    );
}

package bitc.full502.final_project_team1.web.domain.repository;

import bitc.full502.final_project_team1.web.domain.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    List<UserAccount> findTop100ByNameContainingOrUsernameContainingOrderByUserId(
            String nameKeyword, String usernameKeyword
    );
}

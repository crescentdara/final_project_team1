package bitc.full502.final_project_team1.core.domain.repository;

<<<<<<< HEAD
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Integer> {
    Optional<UserAccountEntity> findByUsernameAndStatus(String username, Integer status);
=======
import bitc.full502.final_project_team1.core.domain.entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Integer> {
    List<UserAccountEntity> findTop100ByNameContainingOrUsernameContainingOrderByUserId(
            String nameKeyword, String usernameKeyword
    );
>>>>>>> origin/web/jgy/SurveyList
}

package concat.SolverWeb.user.repository;
import concat.SolverWeb.user.entity.UserSite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserSite, Integer> {
    Optional<UserSite> findByUserId(String userId);
}

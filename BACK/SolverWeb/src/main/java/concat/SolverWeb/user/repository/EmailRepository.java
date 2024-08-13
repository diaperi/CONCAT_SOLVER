package concat.SolverWeb.user.repository;

import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUserEmail(String userEmail);
}
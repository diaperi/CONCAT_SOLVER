package concat.SolverWeb.user.snsLogin.repository;

import concat.SolverWeb.user.snsLogin.entity.SnsUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnsUserRepository extends JpaRepository<SnsUser, Long> {
    SnsUser findByProviderIdAndProvider(String providerId, String provider);
}


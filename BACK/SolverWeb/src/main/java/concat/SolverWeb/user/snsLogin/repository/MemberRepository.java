package concat.SolverWeb.user.snsLogin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import concat.SolverWeb.user.snsLogin.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByLoginId(String loginId);

    Member findByLoginId(String loginId);
}

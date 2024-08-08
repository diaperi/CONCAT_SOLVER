package concat.SolverWeb.user.snslogin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import concat.SolverWeb.user.snslogin.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByLoginId(String loginId);

    Member findByLoginId(String loginId);
}

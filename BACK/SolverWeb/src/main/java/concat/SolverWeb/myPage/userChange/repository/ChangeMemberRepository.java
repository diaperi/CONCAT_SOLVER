package concat.SolverWeb.myPage.userChange.repository;

import concat.SolverWeb.myPage.userChange.entity.ChangeMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChangeMemberRepository extends JpaRepository<ChangeMemberEntity, Long> {
    // 이메일로 회원 정보 조회 (select * from member_table where member_email=?)
    Optional<ChangeMemberEntity> findByMemberEmail(String memberEmail);
}
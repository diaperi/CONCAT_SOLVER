package concat.SolverWeb.user.snslogin.service;

import concat.SolverWeb.user.snslogin.domain.Member;
import concat.SolverWeb.user.snslogin.domain.dto.CustomSecurityUserDetails;
import concat.SolverWeb.user.snslogin.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 로그인 ID로 사용자 정보를 조회
        Member member = memberRepository.findByLoginId(username);

        // 사용자가 존재하지 않는 경우 예외 발생
        if (member == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // 사용자 정보를 기반으로 UserDetails 객체 생성
        return new CustomSecurityUserDetails(member);
    }
}

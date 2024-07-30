package concat.SolverWeb.user.snsLogin.service;

import concat.SolverWeb.user.snsLogin.domain.Member;
import concat.SolverWeb.user.snsLogin.domain.dto.CustomSecurityUserDetails;
import concat.SolverWeb.user.snsLogin.repository.MemberRepository;
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

        Member member = memberRepository.findByLoginId(username);

        if (member != null) {
            return new CustomSecurityUserDetails(member);
        }
        return null;
    }
}

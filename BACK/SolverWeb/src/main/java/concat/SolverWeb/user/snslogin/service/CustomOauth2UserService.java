package concat.SolverWeb.user.snsLogin.service;

import concat.SolverWeb.user.snsLogin.domain.dto.*;
import concat.SolverWeb.user.snsLogin.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import concat.SolverWeb.user.snsLogin.domain.Member;
import concat.SolverWeb.user.snsLogin.domain.MemberRole;
import concat.SolverWeb.snslogin.domain.dto.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("getAttributes : {}",oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = null;

        if(provider.equals("google")){
            log.info("구글 로그인");
            oAuth2UserInfo = new GoogleUserDetails(oAuth2User.getAttributes());

        } else if (provider.equals("kakao")) {
            log.info("카카오 로그인");
            oAuth2UserInfo = new KakaoUserDetails(oAuth2User.getAttributes());
        } else if (provider.equals("naver")) {
            log.info("네이버 로그인");
            oAuth2UserInfo = new NaverUserDetails(oAuth2User.getAttributes());
        }  else if (provider.equals("apple")) {
            log.info("애플 로그인");
            oAuth2UserInfo = new AppleUserDetails(oAuth2User.getAttributes());
        }

        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String loginId = provider + "_" + providerId;
        String name = oAuth2UserInfo.getName();

        Member findMember = memberRepository.findByLoginId(loginId);
        Member member;

        if (findMember == null) {
            member = Member.builder()
                    .loginId(loginId)
                    .name(name)
                    .provider(provider)
                    .providerId(providerId)
                    .role(MemberRole.USER)
                    .build();
            memberRepository.save(member);
        } else{
            member = findMember;
        }

        return new CustomOauth2UserDetails(member, oAuth2User.getAttributes());
    }
}
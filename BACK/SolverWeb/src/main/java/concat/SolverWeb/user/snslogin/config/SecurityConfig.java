package concat.SolverWeb.user.snslogin.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import concat.SolverWeb.user.snslogin.domain.MemberRole;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/oauth-login/admin").hasRole(MemberRole.ADMIN.name())
                        .requestMatchers("/oauth-login/info").authenticated()
                        .requestMatchers("/snslogin/oauth2/code/google", "/snslogin/oauth2/code/kakao", "/snslogin/oauth2/code/naver").permitAll() // OAuth redirect URIs 허용
                        .anyRequest().permitAll()
                )
                .formLogin((auth) -> auth
                        .loginPage("/oauth-login/snslogin")
                        .loginProcessingUrl("/oauth-login/loginProc")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/oauth-login")
                        .failureUrl("/oauth-login?error")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth-login/snslogin")
                        .defaultSuccessUrl("/oauth-login")
                        .failureUrl("/oauth-login/snslogin?error")
                        .permitAll()
                )
                .logout((auth) -> auth
                        .logoutUrl("/oauth-login/logout")
                )
                .csrf((auth) -> auth.disable());

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        String baseUrl = "http://localhost:8098"; // 기본 URL을 명시적으로 설정
        return new InMemoryClientRegistrationRepository(
                this.naverClientRegistration(baseUrl),
                this.googleClientRegistration(baseUrl),
                this.kakaoClientRegistration(baseUrl)
        );
    }

    private ClientRegistration naverClientRegistration(String baseUrl) {
        return ClientRegistration.withRegistrationId("naver")
                .clientId("KeB2I8Od5D0f8tGxtz9h")
                .clientSecret("IbTjnCtNC7")
                .scope("name", "email")
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri("https://openapi.naver.com/v1/nid/me")
                .userNameAttributeName("response")
                .clientName("Naver")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(baseUrl + "/snslogin/oauth2/code/naver")
                .build();
    }

    private ClientRegistration googleClientRegistration(String baseUrl) {
        return ClientRegistration.withRegistrationId("google")
                .clientId("31911528315-rdtqb9sviamqv3c870fbni1sfqg80957.apps.googleusercontent.com")
                .clientSecret("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")
                .scope("email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(baseUrl + "/snslogin/oauth2/code/google")
                .build();
    }

    private ClientRegistration kakaoClientRegistration(String baseUrl) {
        return ClientRegistration.withRegistrationId("kakao")
                .clientId("b0b82cb476ddfaa6ad5516b1b59d82ac")
                .clientSecret("0OWdNCc7o4uozTJkzSrXLGVbNr5xFQXo")
                .scope("account_email", "profile_nickname")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .clientName("Kakao")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(baseUrl + "/snslogin/oauth2/code/kakao")
                .build();
    }
}





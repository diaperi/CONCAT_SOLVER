////package concat.SolverWeb.user.config;
////
////import concat.SolverWeb.user.service.UserService;
////import concat.SolverWeb.user.entity.UserSite;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
////import org.springframework.security.config.annotation.web.builders.WebSecurity;
////import org.springframework.security.core.userdetails.UserDetailsService;
////import org.springframework.security.core.userdetails.UsernameNotFoundException;
////import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
////import org.springframework.security.crypto.password.PasswordEncoder;
////import org.springframework.security.web.SecurityFilterChain;
////
////import java.util.ArrayList;
////
////@Configuration
////public class SecurityConfig {
////
////    private final UserService userService;
////
////    public SecurityConfig(UserService userService) {
////        this.userService = userService;
////    }
////
////    @Bean
////    public PasswordEncoder passwordEncoder() {
////        return new BCryptPasswordEncoder();
////    }
////
////    @Bean
////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
////        http
////                .authorizeHttpRequests(authorizeRequests ->
////                        authorizeRequests
////                                .requestMatchers("/user/login", "/user/sign", "/user/register","/user/find").permitAll()
////                                .anyRequest().authenticated()
////                )
////                .formLogin(formLogin ->
////                        formLogin
////                                .loginProcessingUrl("/user/login")
////                                .defaultSuccessUrl("/user/find", true)
////
////                )
////                ;
////
////        return http.build();
////    }
////
////    @Bean
////    public UserDetailsService userDetailsService() {
////        return username -> {
////            UserSite user = userService.findByUserId(username)
////                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
////            return new org.springframework.security.core.userdetails.User(
////                   user.getUserId(),
////                    user.getUserPw(),
////                    new ArrayList<>()
////            );
////        };
////   }
////}
//package concat.SolverWeb.user.config;
//
//import concat.SolverWeb.user.service.UserService;
//import concat.SolverWeb.user.entity.UserSite;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.core.userdetails.UserDetailsService;
//
//import java.util.ArrayList;
//
//@Configuration
//public class SecurityConfig {
//
//    private final UserService userService;
//
//    public SecurityConfig(UserService userService) {
//        this.userService = userService;
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authorizeRequests ->
//                        authorizeRequests
//                                .requestMatchers("/user/login", "/user/sign", "/user/register").permitAll()
//                                .anyRequest().authenticated()
//                )
//                .formLogin(formLogin ->
//                        formLogin
//                                .loginProcessingUrl("/user/login") // 로그인 처리 URL
//                                .defaultSuccessUrl("/user/find", true) // 로그인 성공 후 이동할 URL
//
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    public UserDetailsService userDetailsService() {
//        return username -> {
//            UserSite user = userService.findByUserId(username)
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//            return new org.springframework.security.core.userdetails.User(
//                    user.getUserId(),
//                    user.getUserPw(),
//                    new ArrayList<>()
//            );
//        };
//    }
//}
package concat.SolverWeb.user.config;

import concat.SolverWeb.user.service.UserService;
import concat.SolverWeb.user.entity.UserSite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/user/login", "/user/sign", "/user/register").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginProcessingUrl("/user/login") // 로그인 처리 URL
                                .defaultSuccessUrl("/user/find", true) // 로그인 성공 후 이동할 URL
                                .usernameParameter("username") // 폼 필드 이름 매핑
                                .passwordParameter("password") // 폼 필드 이름 매핑
                )
                .csrf(withDefaults());
        ;

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserSite user = userService.findByUserId(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return new org.springframework.security.core.userdetails.User(
                    user.getUserId(),
                    user.getUserPw(),
                    new ArrayList<>()
            );
        };
    }
}

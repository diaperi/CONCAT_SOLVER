package concat.SolverWeb.user.controller;

import concat.SolverWeb.user.dto.UserSiteDTO;
import concat.SolverWeb.user.entity.UserSite;
import concat.SolverWeb.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//@Controller
//@RequestMapping("/user")
//public class UserController {
//    //login.html에서 회원가입 버튼을 누르면 register.html로 이동 요청하는~
//    // 회원가입 페이지 이동
//    @GetMapping("/sign")
//    public String signForm() {
//        return "yoonseo/sign"; //register.html  <from>태그 추가해야할 듯 보내는건 sign.html 가입 버튼에서 보내지는 것!
//    }
//
//    // 회원가입 이용약관 페이지 이동
//    @GetMapping("/register")
//    public String registerForm() {
//        return "yoonseo/register";
//    }
//
//    // 로그인 페이지 이동
//    @GetMapping("/login")
//    public String loginForm() {
//        return "yoonseo/login";
//    }
//
//    // 아이디/비밀번호 찾기 페이지 이동
//    @GetMapping("/find")
//    public String findForm() {
//        return "yoonseo/find";
//    }
//
//    // 회원가입 처리
//    @PostMapping("/register")
//    public String registerUser(@ModelAttribute UserSiteDTO userSiteDTO) {
//        // DTO를 엔티티로 변환
//        UserSite userSite = userService.convertToEntity(userSiteDTO);
//
//        // 비밀번호 해싱 및 저장 -> $2a$10$WWISG05f7GGSushMh9gkjujfu/72wZ2xF.XBBmVf5DKdg/a2cjN..이런식으로 저장됨,
//        userService.saveUser(userSite);
//
//        // 성공 페이지로 리다이렉션
//        return "redirect:/user/login";
//    }
//
//    @Autowired
//    private UserService userService;
//    // DTO를 Entity로 변환
//    private UserSite convertToEntity(UserSiteDTO dto) {
//        UserSite userSite = new UserSite();
//        userSite.setUserNo(dto.getUserNo());
//        userSite.setUserId(dto.getUserId());
//        userSite.setUserPw(dto.getUserPw());
//        userSite.setUserEmail(dto.getUserEmail());
//        userSite.setIsVerified(dto.getIsVerified());
//        userSite.setEnrollDate(dto.getEnrollDate());
//        userSite.setUpdateDate(dto.getUpdateDate());
//        userSite.setIsSecession(dto.getIsSecession());
//        return userSite;
//    }
//
//    // 아이디 중복 확인
//    // 이게 일단 뭔지 모르겠지만 아이디가 중복이면 회원가입 폼 작성시 애러 발생하여 가입이 완료 되지 않음..
//    // 폼 작성 시 중복 검사를 통해서
//    @GetMapping("/check-duplicate/{userId}")
//    public ResponseEntity<Map<String, Boolean>> checkDuplicate(@PathVariable String userId) {
//        boolean exists = userService.checkIfUserIdExists(userId);
//        Map<String, Boolean> response = new HashMap<>();
//        response.put("exists", exists);
//        return ResponseEntity.ok(response);
//    }
//
//    //로그인
//}

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 회원가입 페이지 이동
    @GetMapping("/sign")
    public String signForm() {
        return "yoonseo/sign";
    }

    // 회원가입 이용약관 페이지 이동
    @GetMapping("/register")
    public String registerForm() {
        return "yoonseo/register";
    }

    // 로그인 페이지 이동
    @GetMapping("/login")
    public String loginForm() {
        return "yoonseo/login";
    }

    // 아이디/비밀번호 찾기 페이지 이동
    @GetMapping("/find")
    public String findForm() {
        return "yoonseo/find";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserSiteDTO userSiteDTO) {
        UserSite userSite = userService.convertToEntity(userSiteDTO);
        userService.saveUser(userSite);
        return "redirect:/user/login";
    }

    // 로그인 처리
//    @PostMapping("/login")
//    public String loginUser(@RequestParam String userId, @RequestParam String userPw, Model model) {
//        try {
//            UserSite userSite = userService.loginUser(userId, userPw);
//            model.addAttribute("user", userSite);
//            return "user/find"; // 로그인 성공 후 이동할 페이지 (예: 홈 페이지)
//        } catch (BadCredentialsException e) {
//            model.addAttribute("error", "Invalid credentials");
//            return "user/login"; // 로그인 실패 시 로그인 페이지로 돌아감
//        } catch (Exception e) {
//            model.addAttribute("error", e.getMessage());
//            return "/user/login"; // 로그인 실패 시 로그인 페이지로 돌아감
//        }
//    }

    // 아이디 중복 확인
    @GetMapping("/check-duplicate/{userId}")
    public ResponseEntity<Map<String, Boolean>> checkDuplicate(@PathVariable String userId) {
        boolean exists = userService.checkIfUserIdExists(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);


    }

    // 로그인 페이지를 보여주는 메서드


    // 로그인 후 성공 시 이동할 페이지를 보여주는 메서드

}

package concat.SolverWeb.user.yoonseo.controller;

import concat.SolverWeb.user.coolsms.service.notice.LoggedInUserManager;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import concat.SolverWeb.user.yoonseo.repository.UserRepository;
import concat.SolverWeb.user.yoonseo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    private LoggedInUserManager loggedInUserManager;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "yoonseo/login";
    }

    @PostMapping("/register")
    public ModelAndView register(
            @RequestParam("name") String userName,
            @RequestParam("id") String userId,
            @RequestParam("password") String userPw,
            @RequestParam("email") String userEmail) {

        // UserDTO 객체 생성
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(userName);
        userDTO.setUserId(userId);
        userDTO.setUserPw(userPw); // 비밀번호 평문으로 받아서 서비스에서 해시화
        userDTO.setUserEmail(userEmail);
        userDTO.setEnrollDate(LocalDateTime.now());
        userDTO.setUpdateDate(LocalDateTime.now());
        userDTO.setIsSecession('N');
        userDTO.setIsVerified("N");

        // 비밀번호 해시화, 저장
        userService.save(userDTO);

        return new ModelAndView("redirect:/user/login");
    }

    @PostMapping("/login")
    public String login(@ModelAttribute UserDTO userDTO, HttpSession session, Model model) {
        UserDTO loginResult = userService.login(userDTO);
        if (loginResult != null) {
            session.setAttribute("loggedInUser", loginResult);
            session.setAttribute("userEmail", loginResult.getUserEmail()); // 이메일도 세션에 저장

            // LoggedInUserManager에 userId 저장
            loggedInUserManager.setLoggedInUserId(loginResult.getUserId());

            // 로그인 성공 시 로그 추가
            Logger logger = LoggerFactory.getLogger(UserController.class);
            logger.info("로그인 성공 - 세션에 사용자 정보 저장됨: {}", loginResult);

            // 로그인 성공 시 메인 페이지로 리다이렉트
            return "redirect:/main/mainPage";
        } else {
            // login 실패시 똑같이 login페이지
            model.addAttribute("msg", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "yoonseo/login";
        }
    }

    @GetMapping("/register")
    public String registerForm() {
        return "yoonseo/register";
    }


    @GetMapping("/find")
    public String findForm() {
        return "yoonseo/find";
    }

    @GetMapping("/sign")
    public String signForm() {
        return "yoonseo/sign";
    }

    @GetMapping("/check-session")
    public String checkSession(HttpSession session) {
        UserDTO loggedInUser = (UserDTO) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            System.out.println("세션에 저장된 사용자 정보: " + loggedInUser);
        } else {
            System.out.println("세션에 사용자 정보가 설정되어 있지 않음");
        }
        return "redirect:/";
    }

    @PostMapping("/id-check")
    public ResponseEntity<String> checkUserId(@RequestParam("id") String userId) {
        boolean isDuplicate = userService.isUserIdDuplicate(userId);
        if (isDuplicate) {
            return ResponseEntity.ok("exists");
        } else {
            return ResponseEntity.ok("ok");
        }
    }
}

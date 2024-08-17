package concat.SolverWeb.user.yoonseo.controller;

import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.yoonseo.repository.UserRepository;
import concat.SolverWeb.user.yoonseo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ModelAndView register(
            @RequestParam("name") String UserName,
            @RequestParam("id") String userId,
            @RequestParam("password") String userPw,
            @RequestParam("email") String userEmail) {

        UserEntity newUser = new UserEntity();
        newUser.setUserName(UserName);
        newUser.setUserId(userId);
        newUser.setUserPw(userPw); // 비밀번호 암호화 없이 평문 저장
        newUser.setUserEmail(userEmail);
        newUser.setIsVerified(false);
        newUser.setEnrollDate(LocalDateTime.now());
        newUser.setUpdateDate(LocalDateTime.now());
        newUser.setIsSecession('N');

        userRepository.save(newUser);

        return new ModelAndView("redirect:/user/login");
    }


    @PostMapping("/login")
    public String login(@ModelAttribute UserDTO userDTO, HttpSession session){
        UserDTO loginResult = userService.login(userDTO);
        if(loginResult != null){
            // login 성공시 mainpage로 이동
            session.setAttribute("loggedInUser", loginResult);
            session.setAttribute("userEmail", loginResult.getUserEmail()); // 이메일도 세션에 저장
            return "hyeeun/mainpage"; //mainpage로 이동.
        }else{
            // login 실패시 똑같이 login페이지
            return "yoonseo/login";
        }
    }
    @GetMapping("/register")
    public String registerForm() {
        return "yoonseo/register";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "yoonseo/login"; }

    // 아이디/비밀번호 찾기 페이지 이동
    @GetMapping("/find")
    public String findForm() {
        return "yoonseo/find";
    }
     //회원가입 누르면 -> 이용약관 페이지 이동
    @GetMapping("/sign")
    public String signForm() {
        return "yoonseo/sign";
    }


    @GetMapping("/check-session")
    public String checkSession(HttpSession session){
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


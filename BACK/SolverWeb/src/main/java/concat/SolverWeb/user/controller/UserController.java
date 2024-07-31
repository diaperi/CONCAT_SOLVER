package concat.SolverWeb.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {
    //login.html에서 회원가입 버튼을 누르면 register.html로 이동 요청하는~
    // 회원가입 페이지 이동
    @GetMapping("/register")
    public String registerForm() {
        return "yoonseo/register"; //register.html  <from>태그 추가해야할 듯 보내는건 sign.html 가입 버튼에서 보내지는 것!
    }

    // 회원가입 이용약관 페이지 이동
    @GetMapping("/sign")
    public String signForm() {
        return "yoonseo/sign";
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


}

package concat.SolverWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {
    //login.html에서 회원가입 버튼을 누르면 register.html로 이동 요청하는~
    @GetMapping("register.html")
    public String registerFrom(){
        return "yoonseo/register"; //register.html  <from>태그 추가해야할 듯 보내는건 sign.html 가입 버튼에서 보내지는 것!
    }


}

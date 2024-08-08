package concat.SolverWeb.user.snslogin.controller;

import concat.SolverWeb.user.snslogin.domain.Member;
import concat.SolverWeb.user.snslogin.domain.MemberRole;
import concat.SolverWeb.user.snslogin.domain.dto.JoinRequest;
import concat.SolverWeb.user.snslogin.domain.dto.LoginRequest;
import concat.SolverWeb.user.snslogin.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cookie-login")
public class CookieLoginController {

    private final MemberService memberService;

    @GetMapping(value = {"", "/"})
    public String home(@CookieValue(name = "memberId", required = false)
                       Long memberId, Model model) {
        model.addAttribute("loginType", "cookie-login");
        model.addAttribute("pageName", "쿠키 로그인");

        Member loginMember = memberService.getLoginMemberById(memberId);

        if (loginMember != null) {
            model.addAttribute("name", loginMember.getName());
        }

        return "hyeeun/sns/home";
    }

    @GetMapping("/join")
    public String joinPage(Model model) {
        model.addAttribute("loginType", "cookie-login");
        model.addAttribute("pageName", "쿠키 로그인");

        model.addAttribute("joinRequest", new JoinRequest());
        return "hyeeun/sns/join";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute JoinRequest joinRequest,
                       BindingResult bindingResult, Model model) {

        model.addAttribute("loginType", "cookie-login");
        model.addAttribute("pageName", "쿠키 로그인");

        if(memberService.checkLoginIdDuplicate(joinRequest.getLoginId())){
            bindingResult.addError(new FieldError
                    ("joinRequest",
                            "loginId",
                            "ID가 존재합니다."));

        }

        if(!joinRequest.getPassword().equals(joinRequest.getPasswordCheck())){
            bindingResult.addError(new FieldError(
                    "joinRequest",
                    "passwordCheck",
                    "비밀번호가 일치하지 않습니다"));
        }

        if (bindingResult.hasErrors()) {
            return "hyeeun/sns/join";
        }

        memberService.join(joinRequest);
        return "redirect:/cookie-login";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginType", "cookie-login");
        model.addAttribute("pageName", "쿠키 로그인");

        model.addAttribute("loginRequest", new LoginRequest());
        return "hyeeun/sns/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest, BindingResult bindingResult,
                        HttpServletResponse response, Model model) {
        model.addAttribute("loginType", "cookie-login");
        model.addAttribute("pageName", "쿠키 로그인");

        Member member = memberService.login(loginRequest);

        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if(member == null) {
            bindingResult.reject("loginFail", "로그인 아이디 또는 비밀번호가 틀렸습니다.");
        }

        if(bindingResult.hasErrors()) {
            return "hyeeun/sns/login";
        }

        // 로그인 성공 => 쿠키 생성
        Cookie cookie = new Cookie("memberId", String.valueOf(member.getId()));
        cookie.setMaxAge(60 * 60);  // 쿠키 유효 시간 : 1시간
        response.addCookie(cookie);

        return "redirect:/cookie-login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response, Model model) {
        model.addAttribute("loginType", "cookie-login");
        model.addAttribute("pageName", "쿠키 로그인");

        Cookie cookie = new Cookie("memberId", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/cookie-login";
    }

    @GetMapping("/info")
    public String info(@CookieValue(name = "memberId", required = false) Long memberId, Model model) {

        model.addAttribute("loginType", "cookie-login");
        model.addAttribute("pageName", "쿠키 로그인");

        Member loginMember = memberService.getLoginMemberById(memberId);

        if(loginMember == null) {
            return "redirect:/cookie-login/login";
        }

        model.addAttribute("member", loginMember);
        return "hyeeun/sns/info";
    }

    @GetMapping("/admin")
    public String adminPage(@CookieValue(name = "memberId", required = false) Long memberId, Model model) {

        model.addAttribute("loginType", "cookie-login");
        model.addAttribute("pageName", "쿠키 로그인");

        Member loginMember = memberService.getLoginMemberById(memberId);

        if(loginMember == null) {
            return "redirect:/cookie-login/login";
        }

        if(!loginMember.getRole().equals(MemberRole.ADMIN)) {
            return "redirect:/cookie-login";
        }

        return "hyeeun/sns/admin";
    }
}


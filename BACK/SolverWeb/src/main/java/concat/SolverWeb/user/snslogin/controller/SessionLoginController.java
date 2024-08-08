package concat.SolverWeb.user.snslogin.controller;

import concat.SolverWeb.user.snslogin.domain.Member;
import concat.SolverWeb.user.snslogin.domain.MemberRole;
import concat.SolverWeb.user.snslogin.domain.dto.JoinRequest;
import concat.SolverWeb.user.snslogin.domain.dto.LoginRequest;
import concat.SolverWeb.user.snslogin.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/session-login")
public class SessionLoginController {

    private final MemberService memberService;

    @GetMapping(value = {"", "/"})
    public String home(Model model, @SessionAttribute(name = "memberId", required = false) Long memberId) {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션로그인");

        Member loginMember = memberService.getLoginMemberById(memberId);

        if (loginMember != null) {
            model.addAttribute("name", loginMember.getName());
        }

        return "hyeeun/sns/home";
    }

    @GetMapping("/join")
    public String joinPage(Model model) {

        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션로그인");

        // 회원가입을 위해서 model 통해서 joinRequest 전달
        model.addAttribute("joinRequest", new JoinRequest());
        return "hyeeun/sns/join";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute JoinRequest joinRequest,
                       BindingResult bindingResult, Model model) {

        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션로그인");

        // ID 중복 여부 확인
        if (memberService.checkLoginIdDuplicate(joinRequest.getLoginId())) {
            bindingResult.addError(new FieldError("joinRequest", "loginId", "ID가 존재합니다."));
        }


        // 비밀번호 = 비밀번호 체크 여부 확인
        if (!joinRequest.getPassword().equals(joinRequest.getPasswordCheck())) {
            bindingResult.addError(new FieldError("joinRequest", "passwordCheck", "비밀번호가 일치하지 않습니다."));
        }

        // 에러가 존재할 시 다시 join.html로 전송
        if (bindingResult.hasErrors()) {
            return "hyeeun/sns/join";
        }

        // 에러가 존재하지 않을 시 joinRequest 통해서 회원가입 완료
        memberService.join(joinRequest);

        // 회원가입 시 홈 화면으로 이동
        return "redirect:/session-login";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {

        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션로그인");

        model.addAttribute("loginRequest", new LoginRequest());
        return "hyeeun/sns/snslogin";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest, BindingResult bindingResult,
                        HttpServletRequest httpServletRequest, Model model) {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션로그인");

        Member member = memberService.login(loginRequest);

        if (member == null) {
            bindingResult.reject("loginFail", "로그인 아이디 또는 비밀번호가 틀렸습니다.");
        }

        if (bindingResult.hasErrors()) {
            return "hyeeun/sns/snslogin";
        }

        httpServletRequest.getSession().invalidate();
        HttpSession httpSession = httpServletRequest.getSession(true);

        httpSession.setAttribute("memberId", member.getId());
        httpSession.setMaxInactiveInterval(1800);

        return "redirect:/session-login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest httpServletRequest, Model model) {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션로그인");

        HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession != null) {
            httpSession.invalidate();
        }

        return "redirect:/session-login";
    }

    @GetMapping("/info")
    public String memberInfo(@SessionAttribute(name = "memberId", required = false) Long memberId, Model model) {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션로그인");

        Member loginMember = memberService.getLoginMemberById(memberId);

        if (loginMember == null) {
            return "redirect:/session-login/login";
        }

        model.addAttribute("member", loginMember);
        return "hyeeun/sns/info";
    }
    @GetMapping("/admin")
    public String adminPage(@SessionAttribute(name = "memberId", required = false) Long memberId, Model model) {

        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션 로그인");

        Member loginMember = memberService.getLoginMemberById(memberId);

        if(loginMember == null) {
            return "redirect:/session-login/login";
        }

        if(!loginMember.getRole().equals(MemberRole.ADMIN)) {
            return "redirect:/session-login";
        }

        return "hyeeun/sns/admin";
    }
}

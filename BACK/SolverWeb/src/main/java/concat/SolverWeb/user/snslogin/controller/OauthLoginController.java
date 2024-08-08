package concat.SolverWeb.user.snslogin.controller;

import concat.SolverWeb.user.snslogin.domain.dto.JoinRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import concat.SolverWeb.user.snslogin.domain.Member;
import concat.SolverWeb.user.snslogin.domain.dto.LoginRequest;
import concat.SolverWeb.user.snslogin.service.MemberService;

import java.util.Collection;
import java.util.Iterator;

@Controller
@RequiredArgsConstructor
@RequestMapping("/oauth-login")
public class OauthLoginController {

    private final MemberService memberService;

    @GetMapping(value = {"", "/"})
    public String home(Model model) {
        model.addAttribute("loginType", "oauth-login");
        model.addAttribute("pageName", "oauth 로그인");

        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
        GrantedAuthority auth = iter.next();
        String role = auth.getAuthority();

        Member loginMember = memberService.getLoginMemberByLoginId(loginId);

        if (loginMember != null) {
            model.addAttribute("name", loginMember.getName());
        }

        return "hyeeun/sns/home";
    }

    @GetMapping("/join")
    public String joinPage(Model model) {
        model.addAttribute("loginType", "oauth-login");
        model.addAttribute("pageName", "oauth 로그인");
        model.addAttribute("joinRequest", new JoinRequest());

        return "hyeeun/sns/join";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute JoinRequest joinRequest,
                       BindingResult bindingResult, Model model) {
        model.addAttribute("loginType", "oauth-login");
        model.addAttribute("pageName", "oauth 로그인");

        if (memberService.checkLoginIdDuplicate(joinRequest.getLoginId())) {
            bindingResult.addError(new FieldError("joinRequest", "loginId", "ID가 존재합니다."));
        }

        if (!joinRequest.getPassword().equals(joinRequest.getPasswordCheck())) {
            bindingResult.addError(new FieldError("joinRequest", "passwordCheck", "비밀번호가 일치하지 않습니다."));
        }

        if (bindingResult.hasErrors()) {
            return "hyeeun/sns/join";
        }

        memberService.securityJoin(joinRequest);

        return "redirect:/oauth-login";
    }

    @GetMapping("/snslogin")
    public String loginPage(Model model) {
        model.addAttribute("loginType", "oauth-login");
        model.addAttribute("pageName", "oauth 로그인");
        model.addAttribute("loginRequest", new LoginRequest());

        return "hyeeun/sns/snslogin";
    }

    @GetMapping("/info")
    public String memberInfo(Authentication auth, Model model) {
        model.addAttribute("loginType", "oauth-login");
        model.addAttribute("pageName", "oauth 로그인");

        Member loginMember = memberService.getLoginMemberByLoginId(auth.getName());

        model.addAttribute("member", loginMember);
        return "hyeeun/sns/info";
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("loginType", "oauth-login");
        model.addAttribute("pageName", "oauth 로그인");

        return "hyeeun/sns/admin";
    }

    @GetMapping("snslogin/oauth2/code/{provider}")
    public String handleOAuth2Callback(@PathVariable String provider, Model model) {
        // OAuth2 로그인 처리 로직 작성
        // 여기서는 기본적으로 리디렉션만 수행
        return "redirect:/oauth-login"; // 성공 시 리디렉션할 페이지
    }
}


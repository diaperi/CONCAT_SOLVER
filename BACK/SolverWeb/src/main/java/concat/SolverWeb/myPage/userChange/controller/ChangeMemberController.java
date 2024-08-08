package concat.SolverWeb.myPage.userChange.controller;

import concat.SolverWeb.myPage.userChange.dto.ChangeMemberDTO;
import concat.SolverWeb.myPage.userChange.service.ChangeMemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChangeMemberController {
    private final ChangeMemberService memberService;

    @GetMapping("/member/save")
    public String saveForm() {
        return "hyeeun/userchange/save";
    }

    @PostMapping("/member/save")
    public String save(@ModelAttribute ChangeMemberDTO memberDTO) {
        System.out.println("MemberController.save");
        System.out.println("memberDTO = " + memberDTO);
        memberService.save(memberDTO);
        return "hyeeun/userchange/changelogin";
    }

    @GetMapping("/member/login")
    public String loginForm() {
        return "hyeeun/userchange/changelogin";
    }

    @PostMapping("/member/login")
    public String login(@ModelAttribute ChangeMemberDTO memberDTO, HttpSession session) {
        ChangeMemberDTO loginResult = memberService.login(memberDTO);
        if (loginResult != null) {
            session.setAttribute("loginEmail", loginResult.getMemberEmail());
            return "hyeeun/userchange/main";
        } else {
            return "hyeeun/userchange/changelogin";
        }
    }

    @GetMapping("/member/")
    public String findAll(Model model) {
        List<ChangeMemberDTO> memberDTOList = memberService.findAll();
        model.addAttribute("memberList", memberDTOList);
        return "hyeeun/userchange/list";
    }

    @GetMapping("/member/{id}")
    public String findById(@PathVariable Long id, Model model) {
        ChangeMemberDTO memberDTO = memberService.findById(id);
        model.addAttribute("member", memberDTO);
        return "hyeeun/userchange/detail";
    }

    @GetMapping("/member/update")
    public String updateForm(HttpSession session, Model model) {
        String myEmail = (String) session.getAttribute("loginEmail");
        ChangeMemberDTO memberDTO = memberService.updateForm(myEmail);
        model.addAttribute("updateMember", memberDTO);
        return "hyeeun/userchange/update";
    }

    @PostMapping("/member/update")
    public String update(@ModelAttribute ChangeMemberDTO memberDTO) {
        memberService.update(memberDTO);
        return "redirect:/member/" + memberDTO.getId();
    }

    @GetMapping("/member/delete/{id}")
    public String deleteById(@PathVariable Long id) {
        memberService.deleteById(id);
        return "redirect:/member/";
    }

    @GetMapping("/member/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "hyeeun/userchange/changeindex";
    }

    @PostMapping("/member/email-check")
    public @ResponseBody String emailCheck(@RequestParam("memberEmail") String memberEmail) {
        System.out.println("memberEmail = " + memberEmail);
        boolean isEmailAvailable = memberService.isEmailAvailable(memberEmail);
        return isEmailAvailable ? "ok" : "no";
    }

    @PostMapping("/member/password-check")
    public @ResponseBody String passwordCheck(@RequestParam("password") String password, @RequestParam("id") Long id) {
        boolean isPasswordCorrect = memberService.isPasswordCorrect(password, id);
        return isPasswordCorrect ? "ok" : "no";
    }
}










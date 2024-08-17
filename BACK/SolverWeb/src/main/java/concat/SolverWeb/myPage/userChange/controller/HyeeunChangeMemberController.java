package concat.SolverWeb.myPage.userChange.controller;

import concat.SolverWeb.myPage.userChange.service.HyeeunChangeMemberService;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/change")
public class HyeeunChangeMemberController {

    private final HyeeunChangeMemberService memberService;

    private static final Logger logger = LoggerFactory.getLogger(HyeeunChangeMemberController.class);

    // 세션에서 이메일 정보를 가져오는 헬퍼 메소드
    private String getEmailFromSession(HttpSession session) {
        return (String) session.getAttribute("userEmail");
    }


    // 세션에서 로그인된 사용자 정보를 가져오는 헬퍼 메소드
    private UserDTO getLoggedInUserFromSession(HttpSession session) {
        return (UserDTO) session.getAttribute("loggedInUser");
    }

    @GetMapping("/save")
    public String saveForm() {
        return "yoonseo/register"; // 회원가입 폼
    }

    @PostMapping("/save")
    public String save(@ModelAttribute UserDTO userDTO) {
        memberService.save(userDTO);
        return "redirect:/user/login"; // 로그인 페이지로 리다이렉트
    }


    @GetMapping("/login")
    public String loginForm() {
        return "yoonseo/login"; // 로그인 폼
    }

    @PostMapping("/login")
    public String login(@ModelAttribute UserDTO userDTO, HttpSession session) {
        UserDTO loginResult = memberService.login(userDTO);
        if (loginResult != null) {
            String email = loginResult.getUserEmail();
            session.setAttribute("userEmail", email); // 이메일을 세션에 저장
            session.setAttribute("loggedInUser", loginResult); // 사용자 정보 저장

            logger.info("User logged in, email set in session: " + email);
            logger.info("Session attributes after login: userEmail=" + session.getAttribute("userEmail"));

            return "redirect:/change/main"; // 로그인 성공 후 메인 페이지
        } else {
            logger.warn("Login failed for userId: " + userDTO.getUserId());
            return "redirect:/user/login?error"; // 로그인 실패 시 로그인 페이지로
        }
    }

    @GetMapping("/main")
    public String mainPage(HttpSession session, Model model) {
        String userEmail = getEmailFromSession(session);
        logger.info("Retrieved userEmail from session: " + userEmail);
        if (userEmail == null) {
            logger.info("Session does not contain userEmail. Redirecting to login page.");
            return "redirect:/user/login";
        }
        UserDTO loggedInUser = getLoggedInUserFromSession(session);
        if (loggedInUser == null) {
            logger.warn("No logged-in user found in session");
            return "redirect:/user/login";
        }
        model.addAttribute("user", loggedInUser);
        return "hyeeun/mainpage"; // 메인 페이지
    }

    @GetMapping("/update")
    public String updateForm(HttpSession session, Model model) {
        String userEmail = getEmailFromSession(session);
        logger.info("Retrieved userEmail from session: " + userEmail);
        if (userEmail == null) {
            logger.info("Session does not contain userEmail. Redirecting to login page.");
            return "redirect:/user/login";
        } else {
            UserDTO loggedInUser = getLoggedInUserFromSession(session);
            if (loggedInUser == null) {
                logger.warn("No logged-in user found in session");
                return "redirect:/user/login";
            } else {
                logger.info("Session contains logged-in user: " + loggedInUser);
                model.addAttribute("updateUser", loggedInUser);
                return "hyeeun/userchange/hyeeunupdate"; // 업데이트 폼
            }
        }
    }

    @PostMapping("/update/detail")
    public String update(@ModelAttribute("updateUser") UserDTO userDTO, Model model, HttpSession session) {

        // 세션에서 기존 사용자 정보를 가져옵니다
        UserDTO currentUser = getLoggedInUserFromSession(session);

        if (currentUser != null) {
            userDTO.setEnrollDate(currentUser.getEnrollDate()); // 기존의 enrollDate 유지
            userDTO.setIsVerified(currentUser.getIsVerified());
            userDTO.setIsSecession(currentUser.getIsSecession());
        }

        // 현재 시간으로 업데이트일을 설정
        userDTO.setUpdateDate(LocalDateTime.now());
        memberService.update(userDTO);
        session.setAttribute("loggedInUser", userDTO); // 업데이트된 정보를 세션에 반영
        model.addAttribute("user", userDTO);
        logger.info("User updated, redirecting to user detail page: " + userDTO.getUserNo());
        return "hyeeun/userchange/hyeeundetail"; // 업데이트 후 사용자 상세 페이지로 리다이렉트
    }

    @PostMapping("/delete/{id}")
    public @ResponseBody String deleteById(@PathVariable Integer id, HttpSession session) {
        memberService.deleteById(id);  // 실제 DB에서 사용자 삭제
        session.invalidate();  // 세션 무효화
        logger.info("User deleted from database, session invalidated.");
        return "ok";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 로그아웃 시 세션 무효화
        logger.info("User logged out, session invalidated.");
        return "redirect:/user/login"; // 로그아웃 후 로그인 페이지
    }

    @PostMapping("/email-check")
    public @ResponseBody String emailCheck(@RequestParam("userEmail") String userEmail) {
        boolean isEmailAvailable = memberService.isEmailAvailable(userEmail);
        return isEmailAvailable ? "no" : "ok";
    }

    @PostMapping("/password-check")
    public @ResponseBody String passwordCheck(@RequestParam("userPw") String userPw, @RequestParam("userNo") Integer userNo) {
        boolean isPasswordCorrect = memberService.isPasswordCorrect(userPw, userNo);
        return isPasswordCorrect ? "ok" : "no";
    }
}





















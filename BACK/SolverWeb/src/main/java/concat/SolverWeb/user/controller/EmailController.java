package concat.SolverWeb.user.controller;

import concat.SolverWeb.user.service.EmailService;
import concat.SolverWeb.user.service.MemberService;
import concat.SolverWeb.user.dto.MemberDTO;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Random;

@Controller
public class EmailController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private EmailService emailService;

    private static final int PASSWORD_LEN = 6;
    private final Random random = new SecureRandom();

    @GetMapping("/send-email")
    @ResponseBody
    public String sendEmail(@RequestParam String email) {
        System.out.println("Received email request for: " + email);

        // 이메일 주소로 데이터베이스에서 회원 조회
        MemberDTO member = memberService.getMemberByEmail(email);
        if (member != null) {
            System.out.println("Member Email: " + member.getMemberEmail());
            System.out.println("Member ID: " + member.getMemberId());

            // 임시 비밀번호 생성
            String tempPassword = generateTempPassword();

            // 비밀번호 업데이트
            member.setMemberPassword(tempPassword);
            memberService.updateMemberPassword(member);

            String subject = "[SOLVER] 아이디/비밀번호 안내드립니다.";
            String text = member.getMemberName() + " 님, 안녕하세요." + "\n"
                    + "SOLVER에 요청하신 아이디와 비밀번호를 보내드립니다." + "\n"
                    + "로그인 후 반드시 비밀번호를 변경해주세요." + "\n\n"
                    + "아이디: " + member.getMemberId() + "\n"
                    + "비밀번호: " + tempPassword;

            try {
                // 이메일 전송
                emailService.sendSimpleMessage(member.getMemberEmail(), subject, text);
                System.out.println("Email sent to: " + member.getMemberEmail());
                return "Email sent to " + member.getMemberEmail();
            } catch (MessagingException | UnsupportedEncodingException e) {
                // e.printStackTrace();
                return "Failed to send email: " + e.getMessage();
            }
        } else {
            System.out.println("Member not found");
            return "Member not found";
        }
    }

    private String generateTempPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LEN);
        for (int i = 0; i < PASSWORD_LEN; i++) {
            int randIndex = random.nextInt(36);

            char selectedChar;
            if (randIndex < 10) {
                selectedChar = (char) ('0' + randIndex);
            } else {
                selectedChar = (char) ('A' + randIndex - 10);
            }
            password.append(selectedChar);
        }
        return password.toString();
    }
}

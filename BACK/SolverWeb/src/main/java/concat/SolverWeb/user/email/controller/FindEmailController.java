package concat.SolverWeb.user.email.controller;

import concat.SolverWeb.user.email.service.FindEmailService;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;

@Controller
public class FindEmailController {

    private static final Logger logger = LoggerFactory.getLogger(FindEmailController.class);

    @Autowired
    private FindEmailService findEmailService;

    @GetMapping("/send-email")
    @ResponseBody
    public String sendEmail(@RequestParam String email) {

        // 이메일 주소로 데이터베이스에서 사용자 조회
        try {
            UserDTO user = findEmailService.getUserByEmail(email);

            if (user != null) {
                logger.info("User found: {}", user.getUserEmail());

                // 비밀번호 재설정 이메일 전송
                findEmailService.sendPasswordResetEmail(user);
                return "Email sent to: " + user.getUserEmail();
            } else {
                logger.warn("User not found: {}", email);
                return "User not found";
            }
        } catch (MessagingException | UnsupportedEncodingException e) {
            // 이메일 전송 중 예외 발생 시
            logger.error("Failed to send email: {}. Error: {}", email, e.getMessage());
            return "Failed to send email: " + e.getMessage();
        }
    }
}

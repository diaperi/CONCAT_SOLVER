package concat.SolverWeb.user.email.controller;

import concat.SolverWeb.user.email.service.verify.VerifyCodeService;
import concat.SolverWeb.user.email.service.verify.VerifyEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/user/email")
public class VerifyEmailController {

    private static final Logger logger = LoggerFactory.getLogger(FindEmailController.class);

    private final VerifyEmailService verifyEmailService;
    private final VerifyCodeService verifyCodeService;

    public VerifyEmailController(VerifyEmailService verifyEmailService, VerifyCodeService verifyCodeService) {
        this.verifyEmailService = verifyEmailService;
        this.verifyCodeService = verifyCodeService;
    }

    @PostMapping("/send")
    public String sendVerificationEmail(@RequestParam String email) {
        try {
            verifyEmailService.sendVerifyEmail(email);
            logger.info("Verification email: {}", email);
            return "메일 발송 완료";
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Failed to send email: {}", email);
            return "메일 발송 실패: " + e.getMessage();
        }
    }

    @PostMapping("/verify")
    public boolean verifyEmail(@RequestParam String email, @RequestParam String code) {
        return verifyCodeService.checkVerifyCode(email, code);
    }
}

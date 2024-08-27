package concat.SolverWeb.user.email.service.verify;

import concat.SolverWeb.user.email.service.EmailSenderService;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import jakarta.mail.MessagingException;

@Service
public class VerifyEmailService {

    private final EmailSenderService emailSenderService;
    private final VerifyCodeService verifyCodeService;

    public VerifyEmailService(EmailSenderService emailSenderService, VerifyCodeService verifyCodeService) {
        this.emailSenderService = emailSenderService;
        this.verifyCodeService = verifyCodeService;
    }

    // 이메일 전송
    public void sendVerifyEmail(String to) throws MessagingException, UnsupportedEncodingException {
        String subject = createEmailSubject();
        String code = verifyCodeService.generateVerifyCode();
        String text = createEmailText(code);

        emailSenderService.sendSimpleMessage(to, subject, text);
        verifyCodeService.storeVerifyCode(to, code); // 인증번호 저장
    }

    private String createEmailSubject() {
        return "[SOLVER] 회원가입 인증 메일입니다.";
    }

    private String createEmailText(String code) {
        return "<div style='font-family: Arial, sans-serif; font-size: 15px; color: #333;'>"
                + "<h2 style='color: #4461F2;'>SOLVER 인증 메일</h2>"
                + "<p>안녕하세요.</p>"
                + "<p>SOLVER에 요청하신 회원가입 인증번호를 보내드립니다.</p>"
                + "<p>인증번호를 입력 후 가입을 완료해주세요.</p>"
                + "<p>감사합니다.</p><br>"
                + "<p style='font-size: 16px; font-weight: bold;'>인증번호: <span style='color: #4461F2;'>" + code + "</span></p>"
                + "<hr style='border: 0; border-top: 1px solid #eee;'>"
                + "<p style='font-size: 12px; color: #999;'>이 메일은 SOLVER 시스템에 의해 자동으로 발송되었습니다.</p>"
                + "</div>";
    }
}

package concat.SolverWeb.user.email.service.find;

import concat.SolverWeb.user.email.service.EmailSenderService;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class FindEmailService {

    private final EmailSenderService emailSenderService;
    private final FindPasswordService findPasswordService;

    public FindEmailService(EmailSenderService emailSenderService, FindPasswordService findPasswordService) {
        this.emailSenderService = emailSenderService;
        this.findPasswordService = findPasswordService;
    }

    public UserDTO getUserByEmail(String userEmail) {
        return findPasswordService.getUserByEmail(userEmail);
    }

    // 이메일 전송
    public void sendPasswordResetEmail(UserDTO user) throws MessagingException, UnsupportedEncodingException {
        String tempPassword = findPasswordService.generateTempPassword();
        user.setUserPw(tempPassword);
        findPasswordService.updateUserPassword(user);

        String subject = createEmailSubject();
        String text = createEmailText(user, tempPassword);
        emailSenderService.sendSimpleMessage(user.getUserEmail(), subject, text);
    }

    public String createEmailSubject() {
        return "[SOLVER] 아이디/비밀번호 안내 메일입니다.";
    }

    public String createEmailText(UserDTO user, String tempPassword) {
        return "<div style='font-family: Arial, sans-serif; font-size: 16px; color: #333;'>"
                + "<h2 style='color: #4461F2;'>SOLVER 안내 메일</h2>"
                + "<p>안녕하세요, " + user.getUserName() + "님!</p>"
                + "<p>SOLVER에 요청하신 아이디와 임시 비밀번호를 보내드립니다.</p>"
                + "<p>로그인 후 반드시 비밀번호를 변경해주세요.</p>"
                + "<p>감사합니다.</p><br>"
                + "<p style='font-size: 18px; font-weight: bold;'><b>아이디:</b> " + user.getUserId() + "</p>"
                + "<p style='font-size: 18px; font-weight: bold;'><b>임시 비밀번호:</b> <span style='color: #4461F2;'>" + tempPassword + "</span></p>"
                + "<hr style='border: 0; border-top: 1px solid #eee;'>"
                + "<p style='font-size: 12px; color: #999;'>이 메일은 SOLVER 시스템에 의해 자동으로 발송되었습니다.</p>"
                + "</div>";
    }
}

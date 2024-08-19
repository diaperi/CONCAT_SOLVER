package concat.SolverWeb.user.email.service;

import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.yoonseo.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

@Service
public class VerifyEmailService {

    @Value("${server.port}")
    private int serverPort;

    private final UserRepository userRepository;
    private final VerifyMessageService verifyMessageService;

    public VerifyEmailService(UserRepository userRepository, VerifyMessageService verifyMessageService) {
        this.userRepository = userRepository;
        this.verifyMessageService = verifyMessageService;
    }

    // 이메일 제목 생성
    public String createEmailSubject() {
        return "[SOLVER] 회원가입 이메일 인증 안내입니다.";
    }

    // 이메일 본문 생성
    public String createEmailText(UserDTO user, String verifyLink) {
        return "<html><body>"
                + "<p>" + user.getUserName() + " 님, 안녕하세요.</p>"
                + "<p>SOLVER에 오신 것을 환영합니다.</p>"
                + "<p>회원가입 완료를 위해 아래의 버튼을 클릭해주세요.</p>"
                + "<p>감사합니다.</p><br>"
                + "<form action=\"" + verifyLink + "\" method=\"get\">"
                + "<button type=\"submit\" style=\""
                + "padding: 8px 15px; font-size: 14px; color: #fff; "
                + "background-color: #007bff; border: none; border-radius: 5px; "
                + "cursor: pointer;\">이메일 인증</button>"
                + "</form>"
                + "</body></html>";
    }

    // 이메일 인증 링크 전송
    public void sendVerifyEmail(UserDTO userDTO) {
        try {
            // 링크 생성
            String verifyLink = "http://localhost:" + serverPort + "/user/verify/" + userDTO.getUserNo();

            String emailSubject = createEmailSubject();
            String emailText = createEmailText(userDTO, verifyLink);

            verifyMessageService.sendSimpleMessage(userDTO.getUserEmail(), emailSubject, emailText);
        } catch (MessagingException | UnsupportedEncodingException e) {
            // e.printStackTrace();
        }
    }

    // 이메일 인증 처리
    public boolean verifyEmail(Integer userNo) {
        Optional<UserEntity> userEntityOptional = userRepository.findById(userNo);
        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();
            userEntity.setIsVerified(true); // 이메일 인증 완료
            userRepository.save(userEntity);
            return true;
        }
        return false;
    }
}

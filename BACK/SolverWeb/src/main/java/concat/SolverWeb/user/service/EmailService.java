package concat.SolverWeb.user.service;

import concat.SolverWeb.user.utils.PasswordUtil;
import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.repository.EmailRepository;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Random;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private MessageService messageService;

    private static final int PASSWORD_LEN = 6;
    private final Random random = new SecureRandom();

    // 이메일로 사용자 정보 조회
    public UserDTO getUserByEmail(String userEmail) {
        UserEntity user = emailRepository.findByUserEmail(userEmail).orElse(null);
        if (user != null) {
            return convertToDTO(user);
        }
        return null;
    }

    // 임시 비밀번호 생성
    public String generateTempPassword() {
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

    // 임시 비밀번호 업데이트
    public void updateUserPassword(UserDTO userDTO) {
        UserEntity user = emailRepository.findByUserEmail(userDTO.getUserEmail()).orElse(null);
        if (user != null) {
            String hashedPassword = PasswordUtil.encrypt(userDTO.getUserPw());
            user.setUserPw(hashedPassword);
            emailRepository.save(user);
        } else {
            logger.warn("Failed to update password: {}", userDTO.getUserEmail());
        }
    }

    // 이메일 전송
    public void sendPasswordResetEmail(UserDTO user) throws MessagingException, UnsupportedEncodingException {
        // 임시 비밀번호 생성 및 업데이트
        String tempPassword = generateTempPassword();
        user.setUserPw(tempPassword);
        updateUserPassword(user);

        // 이메일 전송
        sendResetEmail(user, tempPassword);
    }

    private void sendResetEmail(UserDTO user, String tempPassword) throws MessagingException, UnsupportedEncodingException {
        String subject = createEmailSubject();
        String text = createEmailText(user, tempPassword);
        messageService.sendSimpleMessage(user.getUserEmail(), subject, text);
    }

    // 이메일 제목
    public String createEmailSubject() {
        return "[SOLVER] 아이디/비밀번호 안내드립니다.";
    }

    // 이메일 본문
    public String createEmailText(UserDTO user, String tempPassword) {
        return user.getUserName() + " 님, 안녕하세요.\n"
                + "SOLVER에 요청하신 아이디와 임시 비밀번호를 보내드립니다.\n"
                + "로그인 후 반드시 비밀번호를 변경해주세요.\n\n"
                + "아이디: " + user.getUserId() + "\n"
                + "비밀번호: " + tempPassword;
    }

    private UserDTO convertToDTO(UserEntity user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserEmail(user.getUserEmail());
        userDTO.setUserName(user.getUserName());
        userDTO.setUserId(user.getUserId());
        return userDTO;
    }
}

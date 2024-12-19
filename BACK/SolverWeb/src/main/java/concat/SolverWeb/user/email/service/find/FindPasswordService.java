package concat.SolverWeb.user.email.service.find;

import concat.SolverWeb.user.utils.PasswordUtil;
import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.email.repository.FindEmailRepository;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Random;

@Service
public class FindPasswordService {

    private static final Logger logger = LoggerFactory.getLogger(FindPasswordService.class);
    private static final int PASSWORD_LEN = 6;

    private final Random random = new SecureRandom();
    private final FindEmailRepository findEmailRepository;

    public FindPasswordService(FindEmailRepository findEmailRepository) {
        this.findEmailRepository = findEmailRepository;
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
    @Transactional
    public void updateUserPassword(UserDTO userDTO) {
        UserEntity user = findEmailRepository.findByUserEmail(userDTO.getUserEmail()).orElse(null);
        if (user != null) {
            String hashedPassword = PasswordUtil.encrypt(userDTO.getUserPw());
            user.setUserPw(hashedPassword);
            findEmailRepository.save(user);
        } else {
            logger.warn("Failed to update password: {}", userDTO.getUserEmail());
        }
    }

    // 사용자 조회
    public UserDTO getUserByEmail(String userEmail) {
        return findEmailRepository.findByUserEmail(userEmail)
                .map(this::convertToDTO)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
    }

    private UserDTO convertToDTO(UserEntity user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserEmail(user.getUserEmail());
        userDTO.setUserName(user.getUserName());
        userDTO.setUserId(user.getUserId());
        return userDTO;
    }
}

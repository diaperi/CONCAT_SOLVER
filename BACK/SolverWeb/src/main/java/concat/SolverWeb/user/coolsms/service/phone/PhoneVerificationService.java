package concat.SolverWeb.user.coolsms.service.phone;

import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.yoonseo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class PhoneVerificationService {

    private static final int CODE_LEN = 6;

    private final CoolSmsService coolSmsService;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(PhoneVerificationService.class);

    @Autowired
    private HttpSession session;

    public PhoneVerificationService(CoolSmsService coolSmsService, UserRepository userRepository) {
        this.coolSmsService = coolSmsService;
        this.userRepository = userRepository;
    }

    // 인증 코드 생성
    public String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%0" + CODE_LEN + "d", random.nextInt((int) Math.pow(10, CODE_LEN)));
    }

    // 전화번호로 인증 코드 전송
    public boolean sendVerificationCode(String phoneNumber) {
        // 인증 코드 생성
        String code = generateVerificationCode();
        logger.info("Generated Code: {}", code);

        // 인증 코드 세션에 저장
        session.setAttribute(phoneNumber, code);

        try {
            // 인증 코드 전송
            boolean isSent = coolSmsService.sendCertificationCode(phoneNumber, code);
            if (!isSent) {
                logger.error("SMS 인증 코드 전송 실패: 전화번호 {}", phoneNumber);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("인증 코드 전송 중 예외 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public boolean saveOrUpdateUserPhone(String userId, String userPhone) {
        // userId를 기반 사용자 검색
        Optional<UserEntity> userOptional = userRepository.findByUserId(userId);

        if (userOptional.isPresent()) {
            // 기존 사용자 정보 업데이트
            UserEntity user = userOptional.get();
            user.setUserPhone(userPhone);  // 전화번호 업데이트
            user.setIsVerified("N");  // 인증 상태 업데이트
            userRepository.save(user);
            logger.info("전화번호가 업데이트 되었습니다.");
            return true;
        } else {
            // 새로운 사용자 추가
            UserEntity newUser = new UserEntity();
            newUser.setUserId(userId);
            newUser.setUserPhone(userPhone);
            userRepository.save(newUser);
            return true;
        }
    }

    public boolean isPhoneNumberDuplicate(String userPhone) {
        Optional<UserEntity> userOptional = userRepository.findByUserPhone(userPhone);
        return userOptional.isPresent();
    }

    // 인증 코드 검증
    public boolean verifyCode(String phoneNumber, String inputCode) {
        // 세션에서 인증 코드 가져오기
        String storedCode = (String) session.getAttribute(phoneNumber);

        if (storedCode != null && storedCode.equals(inputCode)) {
            // 인증 성공 시 사용자 상태 업데이트
            Optional<UserEntity> userOptional = userRepository.findByUserPhone(phoneNumber);

            if (userOptional.isPresent()) {
                UserEntity user = userOptional.get();
                user.setIsVerified("Y");  // 인증 상태 업데이트
                userRepository.save(user); // 사용자 정보 저장
                logger.info("인증이 완료되었습니다.");
                return true;
            } else {
                // 사용자가 존재하지 않으면 실패
                logger.warn("인증된 전화번호와 일치하는 사용자를 찾을 수 없음: {}", phoneNumber);
                return false;
            }
        } else {
            // 인증 실패
            logger.error("인증 코드 불일치: 세션 코드: {}, 입력 코드: {}", storedCode, inputCode);
            return false;
        }
    }

    public boolean checkVerificationStatus(String userId) {
        // userId에 해당하는 사용자 정보를 조회
        Optional<UserEntity> userOptional = userRepository.findByUserId(userId);

        // 사용자 정보가 존재하지 않으면 인증되지 않은 상태로 간주
        if (!userOptional.isPresent()) {
            return false;
        }
        // 인증 상태를 확인
        UserEntity user = userOptional.get();
        return "Y".equals(user.getIsVerified());  // 인증 상태가 "Y"이면 true 반환
    }
}



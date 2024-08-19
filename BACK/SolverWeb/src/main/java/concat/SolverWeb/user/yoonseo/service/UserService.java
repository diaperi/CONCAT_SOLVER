package concat.SolverWeb.user.yoonseo.service;

import concat.SolverWeb.user.email.service.VerifyEmailService;
import concat.SolverWeb.user.utils.PasswordUtil;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.yoonseo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VerifyEmailService verifyEmailService;

    public UserDTO login(UserDTO userDTO) {
        Optional<UserEntity> byUserId = userRepository.findByUserId(userDTO.getUserId());
        if (byUserId.isPresent()) {
            UserEntity userEntity = byUserId.get();
            // 암호화된 비밀번호와 입력된 비밀번호 비교
            if (PasswordUtil.matches(userDTO.getUserPw(), userEntity.getUserPw())) {
                // 로그인 성공 시 인증 여부
                if (userEntity.getIsVerified()) {
                    return UserDTO.toUserDTO(userEntity);
                } else {
                    return null; // 인증되지 않은 계정
                }
            } else {
                return null; // 비밀번호 불일치
            }
        } else {
            return null; // 사용자 없음
        }
    }

    public void save(UserDTO userDTO) {
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
        userEntity.setUserPw(PasswordUtil.encrypt(userDTO.getUserPw()));
        userRepository.save(userEntity);

        userDTO.setUserNo(userEntity.getUserNo());
        verifyEmailService.sendVerifyEmail(userDTO);
    }

    public boolean isUserIdDuplicate(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }
}

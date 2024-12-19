package concat.SolverWeb.user.yoonseo.service;

import concat.SolverWeb.user.utils.PasswordUtil;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.yoonseo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO login(UserDTO userDTO) {
        Optional<UserEntity> byUserId = userRepository.findByUserId(userDTO.getUserId());
        if (byUserId.isPresent()) {
            UserEntity userEntity = byUserId.get();
            // 암호화된 비밀번호와 입력된 비밀번호 비교
            if (PasswordUtil.matches(userDTO.getUserPw(), userEntity.getUserPw())) {
                return UserDTO.toUserDTO(userEntity);
            } else {
                return null; // 비밀번호 불일치
            }
        } else {
            return null; // 사용자 없음
        }
    }

    @Transactional
    public void save(UserDTO userDTO) {
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
        userEntity.setUserPw(PasswordUtil.encrypt(userDTO.getUserPw()));
        userRepository.save(userEntity);
    }

    public boolean isUserIdDuplicate(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }
}

package concat.SolverWeb.myPage.userChange.service;

import concat.SolverWeb.user.utils.PasswordUtil;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.yoonseo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HyeeunChangeMemberService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(HyeeunChangeMemberService.class); // Logger 선언 및 초기화


    // 사용자 저장
    public void save(UserDTO userDTO) {
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
        userRepository.save(userEntity);
    }

    // 사용자 로그인
    public UserDTO login(UserDTO userDTO) {
        Optional<UserEntity> optionalUserEntity = userRepository.findByUserId(userDTO.getUserId());
        if (optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();
            if (userEntity.getUserPw().equals(userDTO.getUserPw())) {
                return UserDTO.toUserDTO(userEntity);
            }
        }
        return null;
    }

    // 모든 사용자 조회
    public List<UserDTO> findAll() {
        List<UserEntity> userEntityList = userRepository.findAll();
        List<UserDTO> userDTOList = new ArrayList<>();
        for (UserEntity userEntity : userEntityList) {
            userDTOList.add(UserDTO.toUserDTO(userEntity));
        }
        return userDTOList;
    }

    // ID로 사용자 조회
    public UserDTO findByUserId(String userId) {
        Optional<UserEntity> optionalUserEntity = userRepository.findByUserId(userId);
        return optionalUserEntity.map(UserDTO::toUserDTO).orElse(null);
    }

    // 이메일로 사용자 조회
    public UserDTO findByUserEmail(String userEmail) {
        Optional<UserEntity> optionalUserEntity = userRepository.findByUserEmail(userEmail);
        return optionalUserEntity.map(UserDTO::toUserDTO).orElse(null);
    }

    // bcrypt 암호화
    public void update(UserDTO userDTO) {
        // DB에서 기존 UserEntity 가져오기
        UserEntity existingUserEntity = userRepository.findByUserId(userDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 비밀번호가 변경된 경우 암호화하여 업데이트
        if (userDTO.getUserPw() != null && !userDTO.getUserPw().isEmpty()) {
            userDTO.setUserPw(PasswordUtil.encrypt(userDTO.getUserPw()));
            logger.info("Password encrypted successfully");
        } else {
            // 비밀번호가 변경되지 않은 경우 기존 비밀번호를 유지
            userDTO.setUserPw(existingUserEntity.getUserPw());
            logger.info("Password not changed");
        }

        // DTO에서 새로운 정보를 가져와 업데이트
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);

        // enrollDate는 기존 엔티티의 값을 그대로 유지
        userEntity.setEnrollDate(existingUserEntity.getEnrollDate());
        userEntity.setIsVerified(existingUserEntity.getIsVerified());
        userEntity.setIsSecession(existingUserEntity.getIsSecession());

        // 수정일 업데이트
        userEntity.setUpdateDate(LocalDateTime.now());

        // 저장
        userRepository.save(userEntity);
    }


    // 사용자 삭제
    public void deleteById(Integer id) {
        userRepository.deleteById(id);  // DB에서 해당 ID를 가진 사용자 삭제
    }

    // 이메일 중복 확인
    public boolean isEmailAvailable(String userEmail) {
        return userRepository.findByUserEmail(userEmail).isPresent();
    }

    // 비밀번호 확인
    public boolean isPasswordCorrect(String password, Integer id) {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(id);
        if (optionalUserEntity.isPresent()) {
            return optionalUserEntity.get().getUserPw().equals(password);
        }
        return false;
    }
}















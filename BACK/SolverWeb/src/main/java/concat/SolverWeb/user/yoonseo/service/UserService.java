package concat.SolverWeb.user.yoonseo.service;

import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.yoonseo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
//
//@RequiredArgsConstructor
public class UserService {
    //이게 맞는건데 주석처리
//    private final UserRepository userRepository;


    public UserDTO login(UserDTO userDTO) {
    /*
        1. 회원이 입력한 userId로 DB에서 조회를 함
        2. DB에서 조회한 비밀번호와 사용자가 입력한 비밀번호가 일치하는지 판단
    */
        Optional<UserEntity> byUserId = userRepository.findByUserId(userDTO.getUserId());
        if(byUserId.isPresent()){
            // 조회 결과가 있다(해당 userId를 가진 회원 정보가 있다)
            UserEntity userEntity = byUserId.get();
            if(userEntity.getUserPw().equals(userDTO.getUserPw())){
                // 비밀번호 일치
                // entity -> dto 변환 후 리턴
                UserDTO dto = UserDTO.toUserDTO(userEntity);
                return dto;
            } else {
                // 비밀번호 불일치
                return null;
            }
        } else {
            // 조회 결과가 없다(해당 userId를 가진 회원이 없다)
            return null;
        }
    }

    public void save(UserDTO userDTO) {
        // DTO -> Entity 변환
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
        userEntity.setUserPw(userDTO.getUserPw()); // 비밀번호를 암호화하지 않고 저장

        // Entity 저장
        userRepository.save(userEntity);
    }

     @Autowired
     private UserRepository userRepository;

    public boolean isUserIdDuplicate(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }





}

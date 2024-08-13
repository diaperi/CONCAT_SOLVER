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
//이것도 잠깐 주석처리
//@RequiredArgsConstructor
public class UserService {
    //이게 맞는건데 주석처리
//    private final UserRepository userRepository;



//    public void save(UserDTO userDTO) {
//        // 1. dto -> entity 변환
//        // 2. repository의 register 메서드 호출
//        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
//        userRepository.save(userEntity);
//        // repository의 register메서드 호출 (조건. entity 객체를 넘겨줘야 함)
//    }

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
     // onblur 아이디 중복체크
//     public String idCheck(String userId) {
//         Optional<UserEntity> byUserId = userRepository. findByUserId(userId);
//         if(byUserId.isPresent()){
//             // 조회결과가 있다 -> 사용할 수 없다.
//             return null;
//         }else{
//             // 조회결과가 없다 -> 사용할 수 있다.
//             return "ok";
//         }
//     }
     @Autowired
     private UserRepository userRepository;

    public boolean isUserIdDuplicate(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }





}

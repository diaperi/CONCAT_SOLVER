package concat.SolverWeb.user.yoonseo.dto;

import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer userNo;  // 사용자 번호
    private String userName; // 사용자 이름
    private String userId;    // 사용자 아이디
    private String userPw;    // 사용자 비밀번호
    private String userEmail; // 사용자 이메일
    private Boolean isVerified; // 인증여부
    private LocalDateTime enrollDate; // 가입일
    private LocalDateTime updateDate; // 수정일
    private Character isSecession;  // 탈퇴여부 (Y or N)

    public static UserDTO toUserDTO(UserEntity userEntity) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserNo(userEntity.getUserNo());
        userDTO.setUserName(userEntity.getUserName());
        userDTO.setUserId(userEntity.getUserId());
        userDTO.setUserPw(userEntity.getUserPw()); // 암호화된 비밀번호를 가져옴
        userDTO.setUserEmail(userEntity.getUserEmail());
        userDTO.setIsVerified(userEntity.getIsVerified());
        userDTO.setEnrollDate(userEntity.getEnrollDate());
        userDTO.setUpdateDate(userEntity.getUpdateDate());
        userDTO.setIsSecession(userEntity.getIsSecession());
        return userDTO;
    }
}

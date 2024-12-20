package concat.SolverWeb.user.yoonseo.entity;

import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_table")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NO", columnDefinition = "INT")
    private Integer userNo; //사용자 번호

    @Column(name = "USER_NAME", nullable = false, length = 50)
    private String userName; // 사용자 이름

    @Column(name = "USER_PHONE") // 폰
    private String userPhone;

    @Column(name = "USER_ID", nullable = false, unique = true, length = 50)
    private String userId; // 사용자 아이디

    @Column(name = "USER_PW", nullable = false, length = 100)
    private String userPw; // 사용자 비밀번호

    @Column(name = "USER_EMAIL", nullable = false, unique = true, length = 100)
    private String userEmail; // 사용자 이메일

    @Column(name = "ENROLL_DATE", columnDefinition = "TIMESTAMP")
    private LocalDateTime enrollDate; // 가입일

    @Column(name = "UPDATE_DATE", columnDefinition = "TIMESTAMP")
    private LocalDateTime updateDate; // 수정일

    @Column(name = "IS_SECESSION", columnDefinition = "CHAR")
    private Character isSecession; // 탈퇴여부// 지금은null값으로 되어있지만Y or N으로 되어야함.

    @Column(name = "IS_Verified")
    private String isVerified;

    public static UserEntity toUserEntity(UserDTO userDTO) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserNo(userDTO.getUserNo()); // 필요에 따라 설정
        userEntity.setUserName(userDTO.getUserName());
        userEntity.setUserPhone(userDTO.getUserPhone()); // 폰
        userEntity.setUserId(userDTO.getUserId());
        userEntity.setUserPw(userDTO.getUserPw());
        userEntity.setUserEmail(userDTO.getUserEmail());
        userEntity.setEnrollDate(userDTO.getEnrollDate());
        userEntity.setUpdateDate(userDTO.getUpdateDate());
        userEntity.setIsSecession(userDTO.getIsSecession());
        userEntity.setIsVerified(userDTO.getIsVerified());
        return userEntity;
    }

}

package concat.SolverWeb.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class UserSite{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NO", columnDefinition = "INT")
    private Integer userNo; //사용자 번호

    @Column(name = "USER_ID", nullable = false, unique = true, length = 50)
    private String userId; // 사용자 아이디

    @Column(name = "USER_PW", nullable = false, length = 100)
    private String userPw; // 사용자 비밀번호

    @Column(name = "USER_EMAIL", nullable = false, unique = true, length = 100)
    private String userEmail; // 사용자 이메일

    @Column(name = "IS_VERIFIED", columnDefinition = "BOOLEAN")
    private Boolean isVerified; // 인증여부

    @Column(name = "ENROLL_DATE", columnDefinition = "TIMESTAMP")
    private LocalDateTime enrollDate; // 가입일

    @Column(name = "UPDATE_DATE", columnDefinition = "TIMESTAMP")
    private LocalDateTime updateDate; // 수정일

    @Column(name = "IS_SECESSION", columnDefinition = "CHAR")
    private Character isSecession; // 탈퇴여부
    // 지금은 null값으로 되어있지만 Y or N으로 되어야함.

}

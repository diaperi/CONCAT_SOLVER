package concat.SolverWeb.user.dto;

import java.time.LocalDateTime;

public class UserSiteDTO {

    private Integer userNo;        // 사용자 번호
    private String userId;         // 사용자 아이디
    private String userPw;         // 사용자 비밀번호
    private String userEmail;      // 사용자 이메일
    private Boolean isVerified;    // 인증여부
    private LocalDateTime enrollDate; // 가입일
    private LocalDateTime updateDate; // 수정일
    private Character isSecession; // 탈퇴여부

    // 기본 생성자
    public UserSiteDTO() {
    }

    // 모든 필드를 초기화하는 생성자
    public UserSiteDTO(Integer userNo, String userId, String userPw, String userEmail, Boolean isVerified, LocalDateTime enrollDate, LocalDateTime updateDate, Character isSecession) {
        this.userNo = userNo;
        this.userId = userId;
        this.userPw = userPw;
        this.userEmail = userEmail;
        this.isVerified = isVerified;
        this.enrollDate = enrollDate;
        this.updateDate = updateDate;
        this.isSecession = isSecession;
    }

    // Getter와 Setter 메소드
    public Integer getUserNo() {
        return userNo;
    }

    public void setUserNo(Integer userNo) {
        this.userNo = userNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPw() {
        return userPw;
    }

    public void setUserPw(String userPw) {
        this.userPw = userPw;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public LocalDateTime getEnrollDate() {
        return enrollDate;
    }

    public void setEnrollDate(LocalDateTime enrollDate) {
        this.enrollDate = enrollDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public Character getIsSecession() {
        return isSecession;
    }

    public void setIsSecession(Character isSecession) {
        this.isSecession = isSecession;
    }

    // toString 메소드
    @Override
    public String toString() {
        return "UserSiteDTO{" +
                "userNo=" + userNo +
                ", userId='" + userId + '\'' +
                ", userPw='" + userPw + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", isVerified=" + isVerified +
                ", enrollDate=" + enrollDate +
                ", updateDate=" + updateDate +
                ", isSecession=" + isSecession +
                '}';
    }

    // equals 메소드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserSiteDTO that = (UserSiteDTO) o;

        return userNo != null ? userNo.equals(that.userNo) : that.userNo == null;
    }

    // hashCode 메소드
    @Override
    public int hashCode() {
        return userNo != null ? userNo.hashCode() : 0;
    }
}

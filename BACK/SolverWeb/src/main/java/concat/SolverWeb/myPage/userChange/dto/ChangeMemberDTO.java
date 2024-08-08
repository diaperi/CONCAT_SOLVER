package concat.SolverWeb.myPage.userChange.dto;

import concat.SolverWeb.myPage.userChange.entity.ChangeMemberEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChangeMemberDTO {
    private Long id;  // 아이디 필드
    private String memberEmail;  // 이메일 필드
    private String memberPassword;  // 비밀번호 필드
    private String memberPasswordConfirm;  // 비밀번호 확인 필드
    private String memberName;  // 이름 필드
    private String memberPhone;  // 전화번호 필드
    private String memberId;  // 사용자 아이디 필드

    public static ChangeMemberDTO toMemberDTO(ChangeMemberEntity memberEntity) {
        ChangeMemberDTO memberDTO = new ChangeMemberDTO();
        memberDTO.setId(memberEntity.getId());
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        memberDTO.setMemberPassword(memberEntity.getMemberPassword());
        memberDTO.setMemberName(memberEntity.getMemberName());
        memberDTO.setMemberPhone(memberEntity.getMemberPhone());
        memberDTO.setMemberId(memberEntity.getMemberId());
        return memberDTO;
    }
}

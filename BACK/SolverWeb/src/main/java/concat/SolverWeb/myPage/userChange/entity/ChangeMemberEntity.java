package concat.SolverWeb.myPage.userChange.entity;

import concat.SolverWeb.myPage.userChange.dto.ChangeMemberDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "member_table")
public class ChangeMemberEntity {
    @Id // pk 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    private Long id;

    @Column(unique = true) // unique 제약조건 추가
    private String memberEmail;

    @Column
    private String memberPassword;

    @Column
    private String memberName;

    @Column
    private String memberPhone;

    @Column
    private String memberId;

    public static ChangeMemberEntity toMemberEntity(ChangeMemberDTO memberDTO) {
        ChangeMemberEntity memberEntity = new ChangeMemberEntity();
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        memberEntity.setMemberPassword(memberDTO.getMemberPassword());
        memberEntity.setMemberName(memberDTO.getMemberName());
        memberEntity.setMemberPhone(memberDTO.getMemberPhone());
        memberEntity.setMemberId(memberDTO.getMemberId());
        return memberEntity;
    }

    public static ChangeMemberEntity toUpdateMemberEntity(ChangeMemberDTO memberDTO) {
        ChangeMemberEntity memberEntity = new ChangeMemberEntity();
        memberEntity.setId(memberDTO.getId());
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        memberEntity.setMemberPassword(memberDTO.getMemberPassword());
        memberEntity.setMemberName(memberDTO.getMemberName());
        memberEntity.setMemberPhone(memberDTO.getMemberPhone());
        memberEntity.setMemberId(memberDTO.getMemberId());
        return memberEntity;
    }
}
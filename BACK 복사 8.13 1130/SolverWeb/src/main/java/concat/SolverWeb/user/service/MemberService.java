package concat.SolverWeb.user.service;

import concat.SolverWeb.user.entity.MemberEntity;
import concat.SolverWeb.user.repository.MemberRepository;
import concat.SolverWeb.user.dto.MemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    public MemberDTO getMemberByEmail(String memberEmail) {
        MemberEntity member = memberRepository.findByMemberEmail(memberEmail)
                .orElse(null);
        if (member != null) {
            return convertToDTO(member);
        }
        return null;
    }

    public void updateMemberPassword(MemberDTO memberDTO) {
        MemberEntity member = memberRepository.findByMemberEmail(memberDTO.getMemberEmail())
                .orElse(null);
        if (member != null) {
            member.setMemberPassword(memberDTO.getMemberPassword());
            memberRepository.save(member);
        }
    }

    private MemberDTO convertToDTO(MemberEntity member) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberEmail(member.getMemberEmail());
        memberDTO.setMemberName(member.getMemberName());
        memberDTO.setMemberId(member.getMemberId());
        memberDTO.setMemberPassword(member.getMemberPassword());
        return memberDTO;
    }
}

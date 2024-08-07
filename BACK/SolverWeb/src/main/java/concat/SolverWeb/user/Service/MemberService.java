package concat.SolverWeb.user.Service;

import concat.SolverWeb.user.Entity.MemberEntity;
import concat.SolverWeb.user.Repository.MemberRepository;
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

    private MemberDTO convertToDTO(MemberEntity member) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberEmail(member.getMemberEmail());
        memberDTO.setMemberName(member.getMemberName());
        memberDTO.setMemberId(member.getMemberId());
        memberDTO.setMemberPassword(member.getMemberPassword());
        return memberDTO;
    }
}

package concat.SolverWeb.user.Service;

import concat.SolverWeb.user.Entity.MemberEntity;
import concat.SolverWeb.user.Repository.MemberRepository;
import concat.SolverWeb.user.dto.MemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public MemberDTO getMemberByEmail(String memberEmail) {
        MemberEntity member = memberRepository.findByMemberEmail(memberEmail)
                .orElse(null);
        if (member != null) {
            return convertToDTO(member);
        }
        return null;
    }

    public void updateMemberPassword(MemberDTO memberDTO) {
        Optional<MemberEntity> memberOptional = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());
        if (memberOptional.isPresent()) {
            MemberEntity member = memberOptional.get();
            // 비밀번호 해싱
            String encodedPassword = passwordEncoder.encode(memberDTO.getMemberPassword());
            member.setMemberPassword(encodedPassword);
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

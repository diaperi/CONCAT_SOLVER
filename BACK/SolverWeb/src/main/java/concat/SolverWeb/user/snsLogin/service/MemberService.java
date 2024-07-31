//package concat.SolverWeb.user.snsLogin.service;
//
//import concat.SolverWeb.user.snsLogin.domain.Member;
//import concat.SolverWeb.user.snsLogin.domain.dto.JoinRequest;
//import concat.SolverWeb.user.snsLogin.domain.dto.LoginRequest;
//import concat.SolverWeb.user.snsLogin.repository.MemberRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class MemberService {
//
//    private final MemberRepository memberRepository;
//    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    public boolean checkLoginIdDuplicate(String loginId){
//        return memberRepository.existsByLoginId(loginId);
//    }
//
//
//    public void join(JoinRequest joinRequest) {
//        memberRepository.save(joinRequest.toEntity());
//    }
//
//    public void securityJoin(JoinRequest joinRequest){
//        if(memberRepository.existsByLoginId(joinRequest.getLoginId())){
//            return;
//        }
//
//        joinRequest.setPassword(bCryptPasswordEncoder.encode(joinRequest.getPassword()));
//
//        memberRepository.save(joinRequest.toEntity());
//    }
//
//    public Member login(LoginRequest loginRequest) {
//        Member findMember = memberRepository.findByLoginId(loginRequest.getLoginId());
//
//        if(findMember == null){
//            return null;
//        }
//
//        if (!bCryptPasswordEncoder.matches(loginRequest.getPassword(), findMember.getPassword())) {
//            return null;
//        }
//
//        return findMember;
//    }
//
//    public Member getLoginMemberById(Long memberId){
//        if(memberId == null) return null;
//
//        Optional<Member> findMember = memberRepository.findById(memberId);
//        return findMember.orElse(null);
//
//    }
//
//    public Member getLoginMemberByLoginId(String loginId){
//        if(loginId == null) return null;
//
//        return memberRepository.findByLoginId(loginId);
//
//    }
//
//}

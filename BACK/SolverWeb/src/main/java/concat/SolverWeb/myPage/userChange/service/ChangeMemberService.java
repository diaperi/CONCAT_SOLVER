package concat.SolverWeb.myPage.userChange.service;

import concat.SolverWeb.myPage.userChange.dto.ChangeMemberDTO;
import concat.SolverWeb.myPage.userChange.entity.ChangeMemberEntity;
import concat.SolverWeb.myPage.userChange.repository.ChangeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChangeMemberService {
    private final ChangeMemberRepository memberRepository;

    public void save(ChangeMemberDTO memberDTO) {
        ChangeMemberEntity memberEntity = ChangeMemberEntity.toMemberEntity(memberDTO);
        memberRepository.save(memberEntity);
    }

    public ChangeMemberDTO login(ChangeMemberDTO memberDTO) {
        Optional<ChangeMemberEntity> byMemberEmail = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());
        if (byMemberEmail.isPresent()) {
            ChangeMemberEntity memberEntity = byMemberEmail.get();
            if (memberEntity.getMemberPassword().equals(memberDTO.getMemberPassword())) {
                return ChangeMemberDTO.toMemberDTO(memberEntity);
            }
        }
        return null;
    }

    public List<ChangeMemberDTO> findAll() {
        List<ChangeMemberEntity> memberEntityList = memberRepository.findAll();
        List<ChangeMemberDTO> memberDTOList = new ArrayList<>();
        for (ChangeMemberEntity memberEntity : memberEntityList) {
            memberDTOList.add(ChangeMemberDTO.toMemberDTO(memberEntity));
        }
        return memberDTOList;
    }

    public ChangeMemberDTO findById(Long id) {
        Optional<ChangeMemberEntity> optionalMemberEntity = memberRepository.findById(id);
        return optionalMemberEntity.map(ChangeMemberDTO::toMemberDTO).orElse(null);
    }

    public ChangeMemberDTO updateForm(String myEmail) {
        Optional<ChangeMemberEntity> optionalMemberEntity = memberRepository.findByMemberEmail(myEmail);
        return optionalMemberEntity.map(ChangeMemberDTO::toMemberDTO).orElse(null);
    }

    public void update(ChangeMemberDTO memberDTO) {
        memberRepository.save(ChangeMemberEntity.toUpdateMemberEntity(memberDTO));
    }

    public void deleteById(Long id) {
        memberRepository.deleteById(id);
    }

    public boolean isEmailAvailable(String memberEmail) {
        return !memberRepository.findByMemberEmail(memberEmail).isPresent();
    }

    public boolean isPasswordCorrect(String password, Long id) {
        Optional<ChangeMemberEntity> optionalMemberEntity = memberRepository.findById(id);
        if (optionalMemberEntity.isPresent()) {
            return optionalMemberEntity.get().getMemberPassword().equals(password);
        }
        return false;
    }
}













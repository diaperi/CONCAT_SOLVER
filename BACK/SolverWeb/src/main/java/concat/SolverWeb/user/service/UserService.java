package concat.SolverWeb.user.service;

import concat.SolverWeb.user.dto.UserSiteDTO;
import concat.SolverWeb.user.entity.UserSite;
import concat.SolverWeb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

//@Service
//public class UserService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//    // 사용자 저장
//    public UserSite saveUser(UserSite userSite) {
//        // 비밀번호 해싱
//        userSite.setUserPw(passwordEncoder.encode(userSite.getUserPw()));
//        userSite.setEnrollDate(LocalDateTime.now());
//        userSite.setUpdateDate(LocalDateTime.now());
//        userSite.setIsVerified(false); // 기본값 설정 (이메일 인증 등으로 변경될 수 있음)
//        return userRepository.save(userSite);
//    }
//
//    // 아이디 중복 확인
//    public boolean checkIfUserIdExists(String userId) {
//        return userRepository.findByUserId(userId).isPresent();
//    }
//
//    // DTO를 Entity로 변환
//    public UserSite convertToEntity(UserSiteDTO dto) {
//        UserSite userSite = new UserSite();
//        userSite.setUserNo(dto.getUserNo());
//        userSite.setUserId(dto.getUserId());
//        userSite.setUserPw(dto.getUserPw());
//        userSite.setUserEmail(dto.getUserEmail());
//        userSite.setIsVerified(dto.getIsVerified());
//        userSite.setEnrollDate(dto.getEnrollDate());
//        userSite.setUpdateDate(dto.getUpdateDate());
//        userSite.setIsSecession(dto.getIsSecession());
//        return userSite;
//    }
//
//    // Entity를 DTO로 변환
//    public UserSiteDTO convertToDTO(UserSite userSite) {
//        UserSiteDTO dto = new UserSiteDTO();
//        dto.setUserNo(userSite.getUserNo());
//        dto.setUserId(userSite.getUserId());
//        dto.setUserPw(userSite.getUserPw());
//        dto.setUserEmail(userSite.getUserEmail());
//        dto.setIsVerified(userSite.getIsVerified());
//        dto.setEnrollDate(userSite.getEnrollDate());
//        dto.setUpdateDate(userSite.getUpdateDate());
//        dto.setIsSecession(userSite.getIsSecession());
//        return dto;
//    }
//
//    public String idCheck(String userId) {
//        // 아이디 존재 여부 확인
//        boolean isAvailable = !userRepository.findByUserId(userId).isPresent();
//
//        if (isAvailable) {
//            return "available"; // 아이디가 사용 가능한 경우
//        } else {
//            return "unavailable"; // 아이디가 이미 사용 중인 경우
//        }
//    }
//
//}

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 사용자 저장
    public UserSite saveUser(UserSite userSite) {
        userSite.setUserPw(passwordEncoder.encode(userSite.getUserPw()));
        userSite.setEnrollDate(LocalDateTime.now());
        userSite.setUpdateDate(LocalDateTime.now());
        userSite.setIsVerified(false); // 기본값 설정
        return userRepository.save(userSite);
    }

    // 아이디 중복 확인
    public boolean checkIfUserIdExists(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }

    // 사용자 ID로 사용자 찾기
    public Optional<UserSite> findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    // DTO를 Entity로 변환
    public UserSite convertToEntity(UserSiteDTO dto) {
        UserSite userSite = new UserSite();
        userSite.setUserNo(dto.getUserNo());
        userSite.setUserId(dto.getUserId());
        userSite.setUserPw(dto.getUserPw());
        userSite.setUserEmail(dto.getUserEmail());
        userSite.setIsVerified(dto.getIsVerified());
        userSite.setEnrollDate(dto.getEnrollDate());
        userSite.setUpdateDate(dto.getUpdateDate());
        userSite.setIsSecession(dto.getIsSecession());
        return userSite;
    }

    // Entity를 DTO로 변환
    public UserSiteDTO convertToDTO(UserSite userSite) {
        UserSiteDTO dto = new UserSiteDTO();
        dto.setUserNo(userSite.getUserNo());
        dto.setUserId(userSite.getUserId());
        dto.setUserPw(userSite.getUserPw());
        dto.setUserEmail(userSite.getUserEmail());
        dto.setIsVerified(userSite.getIsVerified());
        dto.setEnrollDate(userSite.getEnrollDate());
        dto.setUpdateDate(userSite.getUpdateDate());
        dto.setIsSecession(userSite.getIsSecession());
        return dto;
    }

    public String idCheck(String userId) {
        boolean isAvailable = !userRepository.findByUserId(userId).isPresent();
        return isAvailable ? "available" : "unavailable";
    }
    // 사용자 로그인
    public UserSite loginUser(String userId, String userPw) {
        UserSite userSite = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(userPw, userSite.getUserPw())) {
            throw new BadCredentialsException("Invalid password");
        }

        return userSite;
    }
}



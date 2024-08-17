package concat.SolverWeb.user.service;

//import concat.SolverWeb.user.entity.UserEntity;
//import concat.SolverWeb.user.repository.UserRepository;
//import concat.SolverWeb.user.dto.UserDTO;
import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.repository.EmailRepository;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private EmailRepository emailRepository;

    public UserDTO getUserByEmail(String userEmail) {
        UserEntity user = emailRepository.findByUserEmail(userEmail)
                .orElse(null);
        if (user != null) {
            return convertToDTO(user);
        }
        return null;
    }

    public void updateUserPassword(UserDTO userDTO) {
        UserEntity user = emailRepository.findByUserEmail(userDTO.getUserEmail())
                .orElse(null);
        if (user != null) {
            user.setUserPw(userDTO.getUserPw());
            emailRepository.save(user);
        }
    }

    private UserDTO convertToDTO(UserEntity user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserEmail(user.getUserEmail());
        userDTO.setUserName(user.getUserName());
        userDTO.setUserId(user.getUserId());
        userDTO.setUserPw(user.getUserPw());
        return userDTO;
    }
}

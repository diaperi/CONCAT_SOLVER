//package concat.SolverWeb.myPage.userChange.service;
//
//import concat.SolverWeb.myPage.userChange.dto.ChangeUpdateUserDTO;
//import concat.SolverWeb.myPage.userChange.entity.ChangeUser;
//import concat.SolverWeb.myPage.userChange.repository.ChangeUserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//public class ChangeUserService {
//
//    @Autowired
//    private ChangeUserRepository userRepository;
//
//    public boolean checkEmailDuplicate(String email) {
//        return userRepository.findByEmail(email).isPresent();
//    }
//
//    public boolean updateUser(Long userId, ChangeUpdateUserDTO updateUserDTO) {
//        Optional<ChangeUser> optionalUser = userRepository.findById(userId);
//        if (optionalUser.isPresent()) {
//            ChangeUser user = optionalUser.get();
//            if (updateUserDTO.getEmail() != null) {
//                if (checkEmailDuplicate(updateUserDTO.getEmail()) && !updateUserDTO.getEmail().equals(user.getEmail())) {
//                    return false; // Email is already in use
//                }
//                user.setEmail(updateUserDTO.getEmail());
//            }
//            if (updateUserDTO.getPhoneNumber() != null) {
//                user.setPhoneNumber(updateUserDTO.getPhoneNumber());
//            }
//            if (updateUserDTO.getPassword() != null) {
//                if (updateUserDTO.getPassword().equals(updateUserDTO.getConfirmPassword())) {
//                    user.setPassword(updateUserDTO.getPassword()); // Ideally, you should hash the password here
//                } else {
//                    return false; // Passwords do not match
//                }
//            }
//            userRepository.save(user);
//            return true;
//        }
//        return false;
//    }
//}


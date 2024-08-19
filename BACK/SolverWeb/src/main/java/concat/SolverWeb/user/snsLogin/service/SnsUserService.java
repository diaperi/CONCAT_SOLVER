package concat.SolverWeb.user.snsLogin.service;

import concat.SolverWeb.user.snsLogin.dto.SnsUserDTO;
import concat.SolverWeb.user.snsLogin.entity.SnsUser;
import concat.SolverWeb.user.snsLogin.repository.SnsUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SnsUserService {

    @Autowired
    private SnsUserRepository snsUserRepository;

    @Transactional
    public SnsUser saveOrUpdateUser(SnsUserDTO userDTO) {
        SnsUser existingUser = snsUserRepository.findByProviderIdAndProvider(userDTO.getProviderId(), userDTO.getProvider());

        if (existingUser != null) {
            existingUser.setEmail(userDTO.getEmail());
            existingUser.setName(userDTO.getName());
            return snsUserRepository.save(existingUser);
        } else {
            SnsUser newUser = new SnsUser();
            newUser.setEmail(userDTO.getEmail());
            newUser.setName(userDTO.getName());
            newUser.setProvider(userDTO.getProvider());
            newUser.setProviderId(userDTO.getProviderId());
            return snsUserRepository.save(newUser);
        }
    }
}

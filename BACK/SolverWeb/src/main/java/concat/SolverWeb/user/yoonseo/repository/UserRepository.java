package concat.SolverWeb.user.yoonseo.repository;


import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    // 기본적인 CRUD 메서드는 JpaRepository를 통해 자동으로 제공됩니다.
    // 추가적인 메서드 정의도 가능
    Optional<UserEntity> findByUserId(String userId);

}

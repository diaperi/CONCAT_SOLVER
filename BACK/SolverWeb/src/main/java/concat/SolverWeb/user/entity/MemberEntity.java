package concat.SolverWeb.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "test")
public class MemberEntity {
    @Id
    private Long id;
    private String memberEmail;
    private String memberName;
    private String memberId;
    private String memberPassword;
}

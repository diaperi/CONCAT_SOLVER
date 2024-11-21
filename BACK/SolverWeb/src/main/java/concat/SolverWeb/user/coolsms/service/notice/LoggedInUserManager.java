package concat.SolverWeb.user.coolsms.service.notice;

import org.springframework.stereotype.Component;

@Component
public class LoggedInUserManager {
    private String loggedInUserId;

    public String getLoggedInUserId() {
        return loggedInUserId;
    }

    public void setLoggedInUserId(String loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }
}

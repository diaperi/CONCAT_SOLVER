package concat.SolverWeb.user.coolsms.controller;

import concat.SolverWeb.user.coolsms.service.notice.NoticeService;
import concat.SolverWeb.user.snsLogin.dto.SnsUserDTO;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class NoticeController {

    private static final Logger logger = LoggerFactory.getLogger(NoticeController.class);
    private final NoticeService noticeService;
    private final HttpSession session;

    @Autowired
    public NoticeController(NoticeService noticeService, HttpSession session) {
        this.noticeService = noticeService;
        this.session = session;
    }

    @GetMapping("/check-new-file")
    public String checkNewFileInS3() {
        String userId = getLoggedInUserId();
        if (userId == null) {
            return "로그인된 사용자 정보가 없습니다."; // 로그인되지 않은 경우
        }

        noticeService.checkNewFileInS3(); // userId를 전달
        return "새로운 파일이 있는지 확인 중입니다."; // 작업 중 메시지 반환
    }

    // 로그인한 사용자 ID를 가져오는 메서드
    private String getLoggedInUserId() {
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            return null;
        }

        String userId = null;

        if (loggedInUser instanceof UserDTO) {
            UserDTO user = (UserDTO) loggedInUser;
            userId = user.getUserId();
        } else if (loggedInUser instanceof SnsUserDTO) {
            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
            userId = snsUser.getProviderId();
        } else {
            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
            return null; // 예상치 못한 사용자 유형일 때 처리
        }

        logger.info("로그인된 사용자: {}", loggedInUser.toString());
        return userId;
    }
}


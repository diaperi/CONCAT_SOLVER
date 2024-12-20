package concat.SolverWeb.myPage.trashCs.controller;

import concat.SolverWeb.myPage.trashCs.service.TrashService;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import concat.SolverWeb.user.snsLogin.dto.SnsUserDTO;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/myPage")
public class TrashController {

    private static final Logger logger = LoggerFactory.getLogger(TrashController.class);

    @Autowired
    private TrashService trashService;

    // 마이페이지 고객센터 페이지 이동
    @GetMapping("/cs")
    public String cs() {
        return "yuna/cs";
    }

    // faq 페이지
    @GetMapping("/faq")
    public String faq() {
        return "yuna/faq";
    }

    // 마이페이지 휴지통 페이지 이동
    @GetMapping("/trash")
    public String trash() {
        return "yuna/trash";
    }

    @RequestMapping("/trashEmp")
    public String trashEmp() {
        return "yuna/trashEmp";
    }

    // 휴지통으로 이동
    @GetMapping("/moveToTrash")
    public @ResponseBody Map<String, Object> moveToTrash(@RequestParam("videoUrl") String videoUrl, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // 세션에서 로그인된 사용자 정보 가져오기
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            response.put("success", false);
            response.put("error", "로그인된 사용자 정보가 없습니다.");
            return response;
        }

        String userId;
        if (loggedInUser instanceof UserDTO) {
            UserDTO user = (UserDTO) loggedInUser;
            userId = user.getUserId();
        } else if (loggedInUser instanceof SnsUserDTO) {
            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
            userId = snsUser.getProviderId();
        } else {
            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
            response.put("success", false);
            response.put("error", "예상치 못한 사용자 유형입니다.");
            return response;
        }

        logger.info("로그인된 사용자: {}", loggedInUser.toString());

        // 휴지통으로 이동
        boolean success = trashService.moveToTrash(videoUrl, userId);

        response.put("success", success);

        if (success) {
            logger.info("Moved to trash successfully"); // 휴지통 이동 성공
        } else {
            logger.warn("Failed to move to trash"); // 휴지통 이동 실패
        }

        return response;
    }
}
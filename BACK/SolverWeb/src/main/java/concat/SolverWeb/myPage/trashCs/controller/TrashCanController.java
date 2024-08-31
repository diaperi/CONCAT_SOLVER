package concat.SolverWeb.myPage.trashCs.controller;

import concat.SolverWeb.myPage.trashCs.service.TrashCanService;
import concat.SolverWeb.user.snsLogin.dto.SnsUserDTO;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/myPage")
public class TrashCanController {

    private static final Logger logger = LoggerFactory.getLogger(TrashCanController.class);

    @Autowired
    private TrashCanService trashCanService;

    @Value("${cloud.aws.s3.base.url}")
    private String bucketBaseUrl;

    // trash의 영상 목록을 날짜별로 반환
    @GetMapping("/trash/videos")
    public ResponseEntity<Map<String, Object>> getTrashedVideos(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // 세션에서 로그인된 사용자 정보 가져오기
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("세션에서 로그인된 사용자 정보를 찾을 수 없습니다.");
            response.put("success", false);
            response.put("error", "로그인된 사용자 정보가 없습니다.");
            return ResponseEntity.ok(response);
        }

        String userId;
        if (loggedInUser instanceof UserDTO) {
            UserDTO user = (UserDTO) loggedInUser;
            userId = user.getUserId();
            logger.info("로그인된 사용자: {}", userId);
        } else if (loggedInUser instanceof SnsUserDTO) {
            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
            userId = snsUser.getProviderId();
            logger.info("SNS 로그인된 사용자: {}", userId);
        } else {
            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
            response.put("success", false);
            response.put("error", "예상치 못한 사용자 유형입니다.");
            return ResponseEntity.ok(response);
        }

        try {
            Map<String, Object> categorizedVideos = trashCanService.getTrashVideosByDate(userId);
            response.put("success", true);
            response.put("videos", categorizedVideos);
            logger.info("Successfully retrieved trash video list");
        } catch (Exception e) {
            logger.error("Failed to retrieve the trash video list", e); // 영상 목록 가져오기 실패
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // Scheduler 오래된 영상 삭제
    @DeleteMapping("/trash/videos/delete")
    public ResponseEntity<Map<String, Object>> deleteOldTrashVideos(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // 세션에서 로그인된 사용자 정보 가져오기
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("세션에서 로그인된 사용자 정보를 찾을 수 없습니다.");
            response.put("success", false);
            response.put("error", "로그인된 사용자 정보가 없습니다.");
            return ResponseEntity.ok(response);
        }

        String userId;
        if (loggedInUser instanceof UserDTO) {
            UserDTO user = (UserDTO) loggedInUser;
            userId = user.getUserId();
            // logger.info("로그인된 사용자: {}", userId);
        } else if (loggedInUser instanceof SnsUserDTO) {
            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
            userId = snsUser.getProviderId();
            logger.info("SNS 로그인된 사용자: {}", userId);
        } else {
            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
            response.put("success", false);
            response.put("error", "예상치 못한 사용자 유형입니다.");
            return ResponseEntity.ok(response);
        }

        try {
            trashCanService.deleteAllTrashVideos(userId);
            response.put("success", true);
            logger.info("Successfully deleted old video"); // 삭제 성공
        } catch (Exception e) {
            logger.error("Failed to delete old video", e); // 삭제 실패
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // 휴지통 비우기
    @DeleteMapping("/trash/videos/empty")
    public ResponseEntity<Map<String, Object>> emptyTrash(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // 세션에서 로그인된 사용자 정보 가져오기
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("세션에서 로그인된 사용자 정보를 찾을 수 없습니다.");
            response.put("success", false);
            response.put("error", "로그인된 사용자 정보가 없습니다.");
            return ResponseEntity.ok(response);
        }

        String userId;
        if (loggedInUser instanceof UserDTO) {
            UserDTO user = (UserDTO) loggedInUser;
            userId = user.getUserId();
            // logger.info("로그인된 사용자: {}", userId);
        } else if (loggedInUser instanceof SnsUserDTO) {
            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
            userId = snsUser.getProviderId();
            logger.info("SNS 로그인된 사용자: {}", userId);
        } else {
            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
            response.put("success", false);
            response.put("error", "예상치 못한 사용자 유형입니다.");
            return ResponseEntity.ok(response);
        }

        try {
            trashCanService.deleteAllTrashVideos(userId);
            response.put("success", true);
            logger.info("Successfully emptied the trash"); // 비우기 성공
        } catch (Exception e) {
            logger.error("Failed to empty the trash", e); // 비우기 실패
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }


    // 휴지통 영상 복구
    @PostMapping("/trash/videos/recover")
    public ResponseEntity<Map<String, Object>> recoverVideo(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            String videoKey = request.get("videoKey");

            // 세션에서 로그인된 사용자 정보 가져오기
            Object loggedInUser = session.getAttribute("loggedInUser");

            if (loggedInUser == null) {
                logger.info("세션에서 로그인된 사용자 정보를 찾을 수 없습니다.");
                response.put("success", false);
                response.put("error", "로그인된 사용자 정보가 없습니다.");
                return ResponseEntity.ok(response);
            }

            String userId;
            if (loggedInUser instanceof UserDTO) {
                UserDTO user = (UserDTO) loggedInUser;
                userId = user.getUserId();
                // logger.info("로그인된 사용자: {}", userId);
            } else if (loggedInUser instanceof SnsUserDTO) {
                SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
                userId = snsUser.getProviderId();
                logger.info("SNS 로그인된 사용자: {}", userId);
            } else {
                logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
                response.put("success", false);
                response.put("error", "예상치 못한 사용자 유형입니다.");
                return ResponseEntity.ok(response);
            }

            boolean isRecovered = trashCanService.recoverVideo(videoKey, userId);

            if (isRecovered) {
                response.put("success", true);
                logger.info("Successfully recovered video"); // 복구 성공
            } else {
                response.put("success", false);
                response.put("error", "Failed to recover video"); // 복구 실패
            }
        } catch (Exception e) {
            logger.error("Failed to recover video", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // GPT 제목
    @GetMapping("/gptTitle")
    @ResponseBody
    public String getGptTitle(
            @RequestParam("userId") String userId,
            @RequestParam("imageKey") String imageKey,
            HttpSession session) {

        try {
            Object loggedInUser = session.getAttribute("loggedInUser");

            if (loggedInUser == null) {
                logger.info("세션에서 로그인된 사용자 정보를 찾을 수 없습니다.");
                return "redirect:/user/login";
            }

            if (loggedInUser instanceof UserDTO) {
                userId = ((UserDTO) loggedInUser).getUserId();
            } else if (loggedInUser instanceof SnsUserDTO) {
                userId = ((SnsUserDTO) loggedInUser).getProviderId();
            } else {
                logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
                return "redirect:/user/login";
            }
        } catch (Exception e) {
            logger.error("사용자 정보를 세션에서 가져오는 도중 오류 발생", e);
            return "redirect:/user/login";
        }

        return trashCanService.getGptTitle(userId, imageKey);
    }

    @GetMapping("/bucketBaseUrl")
    public ResponseEntity<Map<String, String>> getBucketBaseUrl() {
        Map<String, String> response = new HashMap<>();
        response.put("bucketBaseUrl", bucketBaseUrl);
        return ResponseEntity.ok(response);
    }
}
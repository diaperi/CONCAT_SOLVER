package concat.SolverWeb.main.controller;

import concat.SolverWeb.myPage.myPageMain.service.S3Service;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/main")
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private S3Service s3Service;

    @CrossOrigin(origins = "http://192.168.137.107:5000")
    @GetMapping("/mainPage")
    public String mainPage(HttpSession session, Model model) {

        logger.info("메인 페이지 호출됨");

        // 로그인된 사용자 정보를 세션에서 가져옴
        UserDTO loggedInUser = (UserDTO) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            // 로그인된 사용자가 없을 경우 로그인 페이지로 리다이렉트
            logger.info("세션에 로그인된 사용자 정보가 없습니다.");
            return "redirect:/user/login";
        } else {
            logger.info("세션에 로그인된 사용자 정보가 있습니다.");
        }

        // 사용자 ID를 가져옴
        String userId = loggedInUser.getUserId();
        // 모델에 userId 추가
        model.addAttribute("userId", userId);
        // 로그에 사용자 ID 출력
        logger.info("로그인된 사용자 ID: {}", userId);

        // Flask 서버의 URL에 사용자 ID를 포함하여 전달
        String videoFeedUrl = "http://192.168.137.107:5000/video_feed?user_id=" + userId;

        // 모델에 데이터 추가
        model.addAttribute("videoFeedUrl", videoFeedUrl); // Flask 영상 URL 추가

        // Flask 서버로 유저 ID 전송
        String flaskUrl = "http://192.168.137.107:5000/receive_user_id";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> userIdMap = new HashMap<>();
        userIdMap.put("userId", userId);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(userIdMap, headers);

        try {
            // Flask 서버에 유저 ID 전송 시도
            ResponseEntity<String> response = restTemplate.exchange(flaskUrl, HttpMethod.POST, request, String.class);
            logger.info("Flask 응답: {}", response.getBody());

        } catch (Exception e) {
            // Flask 서버로의 요청 실패 시 예외 처리
            logger.error("Flask 서버로의 요청 실패: ", e);
            // 라즈베리파이 기기와의 통신에 실패하면 다른 페이지로 리다이렉트
            return "hyeeun/mainPageNotLogin";
        }

        return "hyeeun/mainPage";
    }
}

@RestController
@RequestMapping("/api/s3")
class S3DataController {

    private final S3Service s3Service;

    @Autowired
    public S3DataController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping("/latest-transcript")
    public ResponseEntity<String> getLatestTranscript(@RequestParam String userId) {
        // 최신 영상 파일의 타임스탬프를 추출하고 해당 타임스탬프에 맞는 텍스트 파일의 내용을 가져오는 로직
        Optional<S3Service.ImageInfo> latestVideo = s3Service.getLatestVideo(userId);

        if (latestVideo.isPresent()) {
            String videoTimestamp = s3Service.extractTimestampFromVideo(latestVideo.get().getKey());
            Optional<String> latestTranscriptContent = s3Service.getTranscriptByVideoTimestamp(userId, videoTimestamp);

            if (latestTranscriptContent.isPresent()) {
                return ResponseEntity.ok(latestTranscriptContent.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No transcript found.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No video found.");
        }
    }

    @GetMapping("/latest-gpt-response")
    public ResponseEntity<String> getLatestGptResponse(@RequestParam String userId) {
        // 최신 영상 파일의 타임스탬프를 추출하고 해당 타임스탬프에 맞는 GPT 응답 파일의 내용을 가져오는 로직
        Optional<S3Service.ImageInfo> latestVideo = s3Service.getLatestVideo(userId);

        if (latestVideo.isPresent()) {
            String videoTimestamp = s3Service.extractTimestampFromVideo(latestVideo.get().getKey());
            Optional<String> latestGptResponseContent = s3Service.getGptResponseByVideoTimestamp(userId, videoTimestamp);

            if (latestGptResponseContent.isPresent()) {
                return ResponseEntity.ok(latestGptResponseContent.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No GPT response found.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No video found.");
        }
    }
}
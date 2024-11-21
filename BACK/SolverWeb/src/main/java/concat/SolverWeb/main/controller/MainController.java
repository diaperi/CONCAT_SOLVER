package concat.SolverWeb.main.controller;

import concat.SolverWeb.myPage.myPageMain.service.S3Service;
import concat.SolverWeb.user.snsLogin.dto.SnsUserDTO;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.InputStreamReader;


import java.util.Optional;

@Controller
@RequestMapping("/main")
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private S3Service s3Service;

    //    @CrossOrigin(origins = "http://192.168.137.107:5000")
//    @GetMapping("/mainPage")
//    public String mainPage(HttpSession session, Model model) {
//
//        logger.info("메인 페이지 호출됨");
//
//        // 세션에서 로그인된 사용자 정보를 가져옴
//        Object loggedInUser = session.getAttribute("loggedInUser");
//
//        logger.info("세션에서 가져온 사용자 정보: {}", loggedInUser);
//
//        if (loggedInUser == null) {
//            logger.info("세션에 로그인된 사용자 정보가 없습니다. 리다이렉트: /user/login");
//            return "redirect:/user/login";
//        } else {
//            logger.info("세션에 로그인된 사용자 정보가 확인되었습니다: {}", loggedInUser);
//        }
//
//
//        // 세션에 저장된 객체의 타입을 확인
//        String userId;
//        String userName; // 사용자 이름을 추가로 가져옵니다.
//        if (loggedInUser instanceof UserDTO) {
//            // 일반 로그인 사용자 처리
//            UserDTO user = (UserDTO) loggedInUser;
//            userId = user.getUserId();
//            userName = user.getUserName(); // 사용자 이름
//            model.addAttribute("userId", userId);
//            model.addAttribute("userName", userName); // 사용자 이름을 모델에 추가
//        } else if (loggedInUser instanceof SnsUserDTO) {
//            // SNS 로그인 사용자 처리
//            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
//            userId = snsUser.getProviderId();
//            userName = snsUser.getName(); // SNS 사용자의 이름
//            model.addAttribute("userId", userId);
//            model.addAttribute("userName", userName); // 사용자 이름을 모델에 추가
//            model.addAttribute("email", snsUser.getEmail());
//        } else {
//            // 예상치 못한 타입의 객체가 세션에 있는 경우
//            logger.warn("세션에 예상치 못한 타입의 객체가 저장되어 있습니다: {}", loggedInUser.getClass());
//            return "redirect:/user/login";
//        }
//
//        // 사용자 ID를 로그에 출력
//        logger.info("로그인된 사용자 ID: {}", userId);
//
//        // Flask 서버의 URL에 사용자 ID를 포함하여 전달
//        String videoFeedUrl = "http://192.168.137.107:5000/video_feed?user_id=" + userId;
//
//        // 모델에 데이터 추가
//        model.addAttribute("videoFeedUrl", videoFeedUrl); // Flask 영상 URL 추가
//
//        // 타임아웃 설정을 위한 SimpleClientHttpRequestFactory 사용
//        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
//        requestFactory.setConnectTimeout(2000);  // 연결 타임아웃 2초
//        requestFactory.setReadTimeout(2000);  // 읽기 타임아웃 2초
//
//        RestTemplate restTemplate = new RestTemplate(requestFactory);
//
//        // Flask 서버로 유저 ID 전송
//        String flaskUrl = "http://192.168.137.107:5000/receive_user_id";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, String> userIdMap = new HashMap<>();
//        userIdMap.put("userId", userId);
//
//        HttpEntity<Map<String, String>> request = new HttpEntity<>(userIdMap, headers);
//
//        try {
//            // Flask 서버에 유저 ID 전송 시도
//            ResponseEntity<String> response = restTemplate.exchange(flaskUrl, HttpMethod.POST, request, String.class);
//            logger.info("Flask 응답: {}", response.getBody());
//
//        } catch (Exception e) {
//            // Flask 서버로의 요청 실패 시 예외 처리
//            logger.error("Flask 서버로의 요청 실패: ", e);
//            // 라즈베리파이 기기와의 통신에 실패하면 다른 페이지로 리다이렉트
//            return "hyeeun/mainPageNotLogin";
//        }
//
//        // Python 스크립트 실행
//        runPythonScript(userId);
//
//        return "hyeeun/mainPage";
//    }
    @CrossOrigin(origins = "http://192.168.137.107:5000")
    @GetMapping("/mainPage")
    public String mainPage(HttpSession session, Model model) {
        logger.info("메인 페이지 호출됨");

        // 세션에서 로그인된 사용자 정보를 가져옴
        Object loggedInUser = session.getAttribute("loggedInUser");
        logger.info("세션에서 가져온 사용자 정보: {}", loggedInUser);

        if (loggedInUser == null) {
            logger.info("세션에 로그인된 사용자 정보가 없습니다. 리다이렉트: /user/login");
            return "redirect:/user/login";
        }

        String userId;
        String userName;

        if (loggedInUser instanceof UserDTO) {
            UserDTO user = (UserDTO) loggedInUser;
            userId = user.getUserId();
            userName = user.getUserName();
            model.addAttribute("userId", userId);
            model.addAttribute("userName", userName);
        } else if (loggedInUser instanceof SnsUserDTO) {
            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
            userId = snsUser.getProviderId();
            userName = snsUser.getName();
            model.addAttribute("userId", userId);
            model.addAttribute("userName", userName);
            model.addAttribute("email", snsUser.getEmail());
        } else {
            logger.warn("세션에 예상치 못한 타입의 객체가 저장되어 있습니다: {}", loggedInUser.getClass());
            return "redirect:/user/login";
        }

        logger.info("로그인된 사용자 ID: {}", userId);

        // Flask 서버 호출
        String flaskUrl = "http://192.168.137.107:5000/receive_user_id";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> userIdMap = new HashMap<>();
        userIdMap.put("userId", userId);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(userIdMap, headers);

        boolean flaskSuccess = true;
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(flaskUrl, HttpMethod.POST, request, String.class);
            logger.info("Flask 응답: {}", response.getBody());
        } catch (Exception e) {
            logger.error("Flask 서버로의 요청 실패: ", e);
            flaskSuccess = false;
        }

        // Flask 요청 성공 여부에 따라 반환 페이지 설정
        String returnPage = flaskSuccess ? "hyeeun/mainPage" : "hyeeun/mainPageNotLogin";

        // 페이지 반환 후 Python 스크립트 실행
        new Thread(() -> {
            boolean pythonSuccess = runPythonScript(userId);
            if (pythonSuccess) {
                logger.info("Python 스크립트 실행 성공");
            } else {
                logger.warn("Python 스크립트 실행 실패");
            }
        }).start();

        return returnPage;
    }

    private boolean runPythonScript(String userId) {
        String pythonPath = "venv/Scripts/python.exe"; // Windows 경로
        String scriptPath = "src/main/resources/scripts/aiVideoMaker.py";

        try {
            logger.info("Python 스크립트를 실행합니다. Python 경로: {}, 스크립트 경로: {}, 사용자 ID: {}", pythonPath, scriptPath, userId);

            ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, scriptPath, userId);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.info("[Python Script] {}", line);
                    }
                } catch (IOException e) {
                    logger.error("Python 스크립트 stdout 읽기 중 오류 발생: ", e);
                }
            }).start();

            int exitCode = process.waitFor();
            logger.info("Python 스크립트 종료. 종료 코드: {}", exitCode);

            return exitCode == 0; // 성공 여부 반환
        } catch (Exception e) {
            logger.error("Python 스크립트 실행 실패: ", e);
        }
        return false;
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
}


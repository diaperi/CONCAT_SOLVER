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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/main")
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private S3Service s3Service;

//    @CrossOrigin(origins = "http://192.168.137.63:5000")
//    @GetMapping("/mainPage")
//    public String mainPage(HttpSession session, Model model) {
//        logger.info("메인 페이지 호출됨");
//
//        // 세션에서 로그인된 사용자 정보를 가져옴
//        Object loggedInUser = session.getAttribute("loggedInUser");
//        logger.info("세션에서 가져온 사용자 정보: {}", loggedInUser);
//
//        if (loggedInUser == null) {
//            logger.info("세션에 로그인된 사용자 정보가 없습니다. 리다이렉트: /user/login");
//            return "redirect:/user/login";
//        }
//
//        String userId;
//        String userName;
//
//        if (loggedInUser instanceof UserDTO) {
//            UserDTO user = (UserDTO) loggedInUser;
//            userId = user.getUserId();
//            userName = user.getUserName();
//            model.addAttribute("userId", userId);
//            model.addAttribute("userName", userName);
//        } else if (loggedInUser instanceof SnsUserDTO) {
//            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
//            userId = snsUser.getProviderId();
//            userName = snsUser.getName();
//            model.addAttribute("userId", userId);
//            model.addAttribute("userName", userName);
//            model.addAttribute("email", snsUser.getEmail());
//        } else {
//            logger.warn("세션에 예상치 못한 타입의 객체가 저장되어 있습니다: {}", loggedInUser.getClass());
//            return "redirect:/user/login";
//        }
//
//        logger.info("로그인된 사용자 ID: {}", userId);
//
//        model.addAttribute("videoFeedUrl", "http://192.168.137.63:5000/video_feed");
//
//        // Flask 서버 호출
//        String flaskUrl = "http://192.168.137.63:5000/receive_user_id";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, String> userIdMap = new HashMap<>();
//        userIdMap.put("userId", userId);
//
//        HttpEntity<Map<String, String>> request = new HttpEntity<>(userIdMap, headers);
//
//        boolean flaskSuccess = true;
//        try {
//            RestTemplate restTemplate = new RestTemplate();
//            ResponseEntity<String> response = restTemplate.exchange(flaskUrl, HttpMethod.POST, request, String.class);
//            logger.info("Flask 응답: {}", response.getBody());
//        } catch (Exception e) {
//            logger.error("Flask 서버로의 요청 실패: ", e);
//            flaskSuccess = false;
//        }
//
//        // Flask 요청 성공 여부에 따라 반환 페이지 설정
//        String returnPage = flaskSuccess ? "hyeeun/mainPage" : "hyeeun/mainPageNotLogin";
//
//        // 페이지 반환 후 Python 스크립트 실행
//        new Thread(() -> {
//            boolean pythonSuccess = runPythonScript(userId);
//            if (pythonSuccess) {
//                logger.info("Python 스크립트 실행 성공");
//            } else {
//                logger.warn("Python 스크립트 실행 실패");
//            }
//        }).start();
//
//        return returnPage;
//    }

    @CrossOrigin(origins = "http://192.168.137.63:5000")
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

        model.addAttribute("videoFeedUrl", "http://192.168.137.63:5000/video_feed");

        // Flask 서버 호출
        String flaskUrl = "http://192.168.137.63:5000/receive_user_id";
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
            boolean detectConflictSuccess = runPythonScript(userId, "src/main/resources/scripts/detectConflict.py");
            if (detectConflictSuccess) {
                logger.info("detectConflict Python 스크립트 실행 성공");
                boolean aiVideoMakerSuccess = runPythonScript(userId, "src/main/resources/scripts/aiVideoMaker.py");
                if (aiVideoMakerSuccess) {
                    logger.info("aiVideoMaker Python 스크립트 실행 성공");
                } else {
                    logger.warn("aiVideoMaker Python 스크립트 실행 실패");
                }
            } else {
                logger.info("detectConflict 결과: 갈등 상황 아님. 파일 삭제 시작");
                String timestamp = extractTimestampFromLogs();
                if (timestamp != null) {
                    logger.info("삭제할 파일의 타임스탬프: {}", timestamp);
                    s3Service.deleteFiles(timestamp); // S3 파일 삭제 호출
                } else {
                    logger.warn("타임스탬프를 추출할 수 없습니다. 파일 삭제를 건너뜁니다.");
                }
            }
        }).start();

        return returnPage;
    }

    private boolean runPythonScript(String userId, String scriptPath) {
        String pythonPath = "venv/Scripts/python.exe"; // Windows 경로

        try {
            logger.info("Python 스크립트를 실행합니다. Python 경로: {}, 스크립트 경로: {}, 사용자 ID: {}", pythonPath, scriptPath, userId);

            ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, scriptPath, userId);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Python 스크립트의 출력 로그 읽기
            StringBuilder outputLogs = new StringBuilder();
            Thread logReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) { // UTF-8 설정
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.info("[Python Script: {}] {}", scriptPath, line);
                        outputLogs.append(line).append("\n");
                    }
                } catch (IOException e) {
                    logger.error("Python 스크립트 stdout 읽기 중 오류 발생: ", e);
                }
            });
            logReader.start();

            int exitCode = process.waitFor();
            logReader.join(); // 로그 읽기 스레드가 종료될 때까지 대기

            logger.info("Python 스크립트 종료. 스크립트 경로: {}, 종료 코드: {}", scriptPath, exitCode);

            // detectConflict.py에서 "갈등 상황 아님" 확인 후 종료 조건 추가
            if (scriptPath.contains("detectConflict.py") && !outputLogs.toString().contains("갈등 상황입니다")) {
                logger.info("detectConflict 결과: 갈등 상황 아님. 다음 스크립트 실행 중단.");
                return false;
            }

            return exitCode == 0; // 성공 여부 반환
        } catch (Exception e) {
            logger.error("Python 스크립트 실행 실패. 스크립트 경로: {}", scriptPath, e);
        }
        return false;
    }


    private String extractTimestampFromLogs() {
        try {
            // 로그 파일을 읽고 특정 패턴에서 타임스탬프 추출 (예: 20241123_072509)
            String exampleLog = "[Python Script: src/main/resources/scripts/detectConflict.py] 새로운 emotion_log: sd/done/emotion_log_20241123_072509.txt";
            String timestampPattern = "emotion_log_(\\d{8}_\\d{6})";

            // 정규식 패턴 매칭
            Pattern pattern = Pattern.compile(timestampPattern);
            Matcher matcher = pattern.matcher(exampleLog);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            logger.error("로그에서 타임스탬프 추출 실패: ", e);
        }
        return null;
    }

}


//    private boolean runPythonScript(String userId) {
//        String pythonPath = "venv/Scripts/python.exe"; // Windows 경로
//        String scriptPath = "src/main/resources/scripts/aiVideoMaker.py";
//
//        try {
//            logger.info("Python 스크립트를 실행합니다. Python 경로: {}, 스크립트 경로: {}, 사용자 ID: {}", pythonPath, scriptPath, userId);
//
//            ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, scriptPath, userId);
//            processBuilder.redirectErrorStream(true);
//            Process process = processBuilder.start();
//
//            new Thread(() -> {
//                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        logger.info("[Python Script] {}", line);
//                    }
//                } catch (IOException e) {
//                    logger.error("Python 스크립트 stdout 읽기 중 오류 발생: ", e);
//                }
//            }).start();
//
//            int exitCode = process.waitFor();
//            logger.info("Python 스크립트 종료. 종료 코드: {}", exitCode);
//
//            return exitCode == 0; // 성공 여부 반환
//        } catch (Exception e) {
//            logger.error("Python 스크립트 실행 실패: ", e);
//        }
//        return false;
//    }


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


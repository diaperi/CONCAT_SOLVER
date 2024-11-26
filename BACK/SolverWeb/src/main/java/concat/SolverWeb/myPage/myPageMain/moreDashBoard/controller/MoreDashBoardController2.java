package concat.SolverWeb.myPage.myPageMain.moreDashBoard.controller;

import concat.SolverWeb.myPage.myPageMain.controller.MyPageController;
import concat.SolverWeb.myPage.myPageMain.service.S3Service;
import concat.SolverWeb.user.snsLogin.dto.SnsUserDTO;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/moreDashBoard")
public class MoreDashBoardController2 {

    private static final Logger logger = LoggerFactory.getLogger(MyPageController.class);
    private final S3Service s3Service;

    public MoreDashBoardController2(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping("/gpt-titles-by-date")
    public CompletableFuture<ResponseEntity<List<Map<String, String>>>> getGptTitlesByDateRange(
            HttpSession session,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 세션에서 사용자 정보 확인
                Object loggedInUser = session.getAttribute("loggedInUser");
                if (loggedInUser == null) {
                    logger.warn("로그인된 사용자 정보가 없습니다.");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }

                // 페이지 및 크기 파라미터 검증
                if (page < 1 || size < 1) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Collections.singletonList(Map.of("error", "Invalid page or size parameter")));
                }

                // 사용자 ID 추출
                String userId = getUserId(loggedInUser);

                // S3에서 해당 날짜 범위와 페이지에 맞는 GPT 제목 가져오기
                List<Map<String, String>> titles = s3Service.getGptTitlesByDateRange(userId, startDate, endDate, page, size);

                if (titles.isEmpty()) {
                    logger.warn("해당 범위에 데이터가 없습니다: {} ~ {}", startDate, endDate);
                }

                return ResponseEntity.ok(titles);

            } catch (Exception e) {
                logger.error("Error occurred while fetching GPT titles", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        });
    }

    @GetMapping("/fetch-emotion-analysis")
    public ResponseEntity<Map<String, String>> fetchEmotionAnalysis(@RequestParam String gptFileKey, HttpSession session) {
        try {
            Object loggedInUser = session.getAttribute("loggedInUser");
            if (loggedInUser == null) {
                return ResponseEntity.status(401).build();
            }

            String userId = getUserId(loggedInUser);

            // GPT 파일 키에서 타임스탬프 추출
            String timestamp = extractTimestampFromGptFile(gptFileKey);
            if (timestamp == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Invalid GPT file key format."));
            }

            // 타임스탬프 로그 출력
            logger.info("Fetched timestamp: {}", timestamp);

            // S3에서 gpt_response와 일치하는 negative_emotion 텍스트 파일 경로 설정
            String s3KeyTranscript = String.format("%s/done/negative_emotion_%s_transcript.txt", userId, timestamp);

            String outputImagePathParticipant1 = String.format("src/main/resources/static/images/%s_participant1_%s_emotion_chart.png", userId, timestamp);
            String outputImagePathParticipant2 = String.format("src/main/resources/static/images/%s_participant2_%s_emotion_chart.png", userId, timestamp);

            // Python 스크립트를 호출하여 감정 분석을 수행합니다.
            String participant1Image = s3Service.generateImageEmotionAnalysis(s3KeyTranscript, "참여자1", outputImagePathParticipant1);
            String participant2Image = s3Service.generateImageEmotionAnalysis(s3KeyTranscript, "참여자2", outputImagePathParticipant2);

            if (participant1Image != null && participant2Image != null) {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("participant1Image", String.format("/images/%s_participant1_%s_emotion_chart.png", userId, timestamp));
                responseMap.put("participant2Image", String.format("/images/%s_participant2_%s_emotion_chart.png", userId, timestamp));
                responseMap.put("date", timestamp.substring(0, 8)); // 날짜 정보 추가 (YYYYMMDD 형식)
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(500).build();
            }
        } catch (Exception e) {
            logger.error("Error during emotion analysis", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 타임스탬프를 추출하는 메서드
    private String extractTimestampFromGptFile(String gptKey) {
        try {
            Pattern pattern = Pattern.compile("gpt_response_(\\d{8})_(\\d{6})\\.txt");
            Matcher matcher = pattern.matcher(gptKey);
            if (matcher.find()) {
                String timestamp = matcher.group(1) + "_" + matcher.group(2); // YYYYMMDD_HHMMSS 형식
                logger.info("Extracted timestamp: {}", timestamp);
                return timestamp;
            } else {
                logger.error("Failed to match GPT key pattern: {}", gptKey);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error extracting timestamp from GPT key: {}", gptKey, e);
            return null;
        }
    }

    private String getUserId(Object loggedInUser) {
        if (loggedInUser instanceof UserDTO) {
            return ((UserDTO) loggedInUser).getUserId();
        } else if (loggedInUser instanceof SnsUserDTO) {
            return ((SnsUserDTO) loggedInUser).getProviderId();
        } else {
            throw new IllegalArgumentException("Unexpected user type");
        }
    }


    @GetMapping("/process-dialogue")
    public ResponseEntity<Map<String, String>> processDialogue(
            @RequestParam String date, HttpSession session) {
        try {
            Object loggedInUser = session.getAttribute("loggedInUser");
            if (loggedInUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userId = getUserId(loggedInUser);

            // 서비스 메서드 호출하여 대화 재구성 수행
            Map<String, String> result = s3Service.processDialogue(userId, date);

            if (result != null && !result.isEmpty()) {
                logger.info("Dialogue processing completed successfully.");
                return ResponseEntity.ok(result);
            } else {
                logger.error("Error: Received null or empty result from processDialogue method.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to process dialogue."));
            }
        } catch (Exception e) {
            logger.error("Error during processing dialogue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while processing dialogue."));
        }
    }
}

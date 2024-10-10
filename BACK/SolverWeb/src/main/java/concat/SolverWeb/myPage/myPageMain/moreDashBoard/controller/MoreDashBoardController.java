package concat.SolverWeb.myPage.myPageMain.moreDashBoard.controller;

import concat.SolverWeb.myPage.myPageMain.controller.MyPageController;
import concat.SolverWeb.myPage.myPageMain.service.S3Service;
import concat.SolverWeb.user.snsLogin.dto.SnsUserDTO;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/moreDashBoard")
public class MoreDashBoardController {

    private static final Logger logger = LoggerFactory.getLogger(MyPageController.class);
    private final S3Service s3Service;

    public MoreDashBoardController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping("/dashBoard")
    public String myPageMain(HttpSession session, Model model) {
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            return "redirect:/user/login";
        }

        String userId;
        String userName;
        if (loggedInUser instanceof UserDTO) {
            UserDTO user = (UserDTO) loggedInUser;
            userId = user.getUserId();
            userName = user.getUserName(); // 사용자 이름 가져오기
        } else if (loggedInUser instanceof SnsUserDTO) {
            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
            userId = snsUser.getProviderId();
            userName = snsUser.getName(); // SNS 사용자 이름 가져오기
        } else {
            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
            return "redirect:/user/login";
        }

        logger.info("로그인된 사용자: {}", loggedInUser.toString());

        // S3에서 사용자별 최신 텍스트 파일 경로 가져오기
        Optional<String> transcriptKeyOpt = s3Service.getLatestTranscript(userId);

        // S3에서 사용자별 최신 이미지 파일 정보 가져오기
        Optional<S3Service.ImageInfo> latestImageOpt = s3Service.getLatestImage(userId);

        // 사용자 이름과 ID를 모델에 추가
        model.addAttribute("userId", userId);
        model.addAttribute("userName", userName);

        // 최신 텍스트 파일 경로를 모델에 추가
        transcriptKeyOpt.ifPresent(transcriptKey -> model.addAttribute("latestTranscript", transcriptKey));

        // 최신 이미지 파일 정보(있을 경우)를 모델에 추가
        latestImageOpt.ifPresent(imageInfo -> model.addAttribute("latestImageUrl", imageInfo.getUrl()));

        return "seoyun/dashBoard"; // 대시보드 뷰 리턴
    }


    // S3 키에서 타임스탬프를 추출하는 메서드
    private String extractTimestampFromS3Key(String s3Key) {
        Pattern pattern = Pattern.compile("negative_emotion_(\\d{8})_(\\d{6})_transcript\\.txt");
        Matcher matcher = pattern.matcher(s3Key);
        if (matcher.find()) {
            return matcher.group(1) + "_" + matcher.group(2); // YYYYMMDD_HHmmss 형식
        } else {
            logger.error("S3 키에서 타임스탬프를 추출하지 못했습니다: " + s3Key);
            return null;
        }
    }


    @GetMapping("/text")
    public String text(HttpSession session, Model model) {
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            return "redirect:/user/login";
        }

        String userId;
        String userName;
        if (loggedInUser instanceof UserDTO) {
            UserDTO user = (UserDTO) loggedInUser;
            userId = user.getUserId();
            userName = user.getUserName(); // 사용자 이름 가져오기
        } else if (loggedInUser instanceof SnsUserDTO) {
            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
            userId = snsUser.getProviderId();
            userName = snsUser.getName(); // SNS 사용자 이름 가져오기
        } else {
            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
            return "redirect:/user/login";
        }

        logger.info("로그인된 사용자: {}", loggedInUser.toString());

        // S3에서 사용자별 최신 텍스트 파일 경로 가져오기
        Optional<String> transcriptKeyOpt = s3Service.getLatestTranscript(userId);


        // 사용자 이름과 ID를 모델에 추가
        model.addAttribute("userId", userId);
        model.addAttribute("userName", userName);

        return "seoyun/text";
    }


    @GetMapping("/emotion")
    public String emotion(HttpSession session, Model model) {
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            return "redirect:/user/login";
        }

        String userId;
        String userName;
        if (loggedInUser instanceof UserDTO) {
            UserDTO user = (UserDTO) loggedInUser;
            userId = user.getUserId();
            userName = user.getUserName(); // 사용자 이름 가져오기
        } else if (loggedInUser instanceof SnsUserDTO) {
            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
            userId = snsUser.getProviderId();
            userName = snsUser.getName(); // SNS 사용자 이름 가져오기
        } else {
            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
            return "redirect:/user/login";
        }

        logger.info("로그인된 사용자: {}", loggedInUser.toString());

        // S3에서 사용자별 최신 텍스트 파일 경로 가져오기
        Optional<String> transcriptKeyOpt = s3Service.getLatestTranscript(userId);

        if (transcriptKeyOpt.isPresent()) {
            String s3Key = transcriptKeyOpt.get();

            // 파일에서 추출한 타임스탬프
            String extractedTimestamp = extractTimestampFromS3Key(s3Key);

            // 현재 시간 타임스탬프
            String currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String outputImagePathParticipant1 = String.format("src/main/resources/static/images/%s_participant1_%s_emotion_chart.png", userId, currentTimestamp);
            String outputImagePathParticipant2 = String.format("src/main/resources/static/images/%s_participant2_%s_emotion_chart.png", userId, currentTimestamp);

            // Python 스크립트를 사용하여 감정 분석 수행
            String participant1ImagePath = s3Service.generateImageEmotionAnalysis(s3Key, "참여자1", outputImagePathParticipant1);
            String participant2ImagePath = s3Service.generateImageEmotionAnalysis(s3Key, "참여자2", outputImagePathParticipant2);

            String textEmotionAnalysisResult = null;
            try {
                // 추가된 텍스트 기반 감정 분석 스크립트 실행
                textEmotionAnalysisResult = s3Service.generateTextEmotionAnalysis(s3Key);
            } catch (Exception e) {
                logger.error("텍스트 감정 분석에서 오류가 발생했습니다.", e);
                model.addAttribute("error", "텍스트 감정 분석 중 오류가 발생했습니다.");
            }

            if (participant1ImagePath != null && participant2ImagePath != null) {
                // 분석된 감정 결과 이미지 URL과 텍스트 결과를 모델에 추가
                model.addAttribute("participant1EmotionChartImageUrl", String.format("/images/%s_participant1_%s_emotion_chart.png", userId, currentTimestamp));
                model.addAttribute("participant2EmotionChartImageUrl", String.format("/images/%s_participant2_%s_emotion_chart.png", userId, currentTimestamp));
                if (textEmotionAnalysisResult != null) {
                    model.addAttribute("textEmotionAnalysisResult", textEmotionAnalysisResult);
                } else {
                    model.addAttribute("textEmotionAnalysisResult", "텍스트 감정 분석 결과를 가져올 수 없습니다.");
                }
                model.addAttribute("timestamp", currentTimestamp); // 현재 시간 타임스탬프
                model.addAttribute("extractedTimestamp", extractedTimestamp); // 파일에서 추출한 타임스탬프
            } else {
                model.addAttribute("error", "감정 분석을 수행하는 중 오류가 발생했습니다.");
            }
        } else {
            model.addAttribute("error", "텍스트 파일을 가져오지 못했습니다.");
        }

        // 사용자 이름과 ID를 모델에 추가
        model.addAttribute("userId", userId);
        model.addAttribute("userName", userName);

        return "seoyun/emotion";
    }
}
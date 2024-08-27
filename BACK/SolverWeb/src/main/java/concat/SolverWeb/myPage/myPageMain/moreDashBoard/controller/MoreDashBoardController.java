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

        if (transcriptKeyOpt.isPresent()) {
            String s3Key = transcriptKeyOpt.get();

            // 동적으로 사용자 ID와 시간을 기반으로 이미지 파일명 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String outputImagePathParticipant1 = String.format("src/main/resources/static/images/%s_participant1_%s_emotion_chart.png", userId, timestamp);
            String outputImagePathParticipant2 = String.format("src/main/resources/static/images/%s_participant2_%s_emotion_chart.png", userId, timestamp);

            // Python 스크립트를 사용하여 감정 분석 수행
            String participant1ImagePath = s3Service.generateEmotionAnalysis(s3Key, "참여자1", outputImagePathParticipant1);
            String participant2ImagePath = s3Service.generateEmotionAnalysis(s3Key, "참여자2", outputImagePathParticipant2);

            if (participant1ImagePath != null && participant2ImagePath != null) {
                // 분석된 감정 결과 이미지 URL을 모델에 추가
                model.addAttribute("participant1EmotionChartImageUrl", String.format("/images/%s_participant1_%s_emotion_chart.png", userId, timestamp));
                model.addAttribute("participant2EmotionChartImageUrl", String.format("/images/%s_participant2_%s_emotion_chart.png", userId, timestamp));
                model.addAttribute("timestamp", timestamp); // Thymeleaf에서 사용할 수 있도록 모델에 추가
            } else {
                model.addAttribute("error", "감정 분석을 수행하는 중 오류가 발생했습니다.");
            }
        } else {
            model.addAttribute("error", "텍스트 파일을 가져오지 못했습니다.");
        }

        // 사용자 이름과 ID를 모델에 추가
        model.addAttribute("userId", userId);
        model.addAttribute("userName", userName);

        return "seoyun/moreDashBoard";
    }
}

package concat.SolverWeb.main.controller;

import concat.SolverWeb.user.yoonseo.controller.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/main")
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);


    //  메인 페이지 이동
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

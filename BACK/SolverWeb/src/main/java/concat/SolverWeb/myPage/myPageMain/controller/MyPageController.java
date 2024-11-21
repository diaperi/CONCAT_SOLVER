package concat.SolverWeb.myPage.myPageMain.controller;

import concat.SolverWeb.myPage.myPageMain.service.S3Service;
import concat.SolverWeb.myPage.myPageMain.service.S3Service.ImageInfo;
import concat.SolverWeb.user.snsLogin.dto.SnsUserDTO;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Controller
//@RequestMapping("/myPage")
//public class MyPageController {
//
//    @Autowired
//    private S3Service s3Service;
//    private static final Logger logger = LoggerFactory.getLogger(MyPageController.class);
//
//    // 마이페이지 이동
//    @GetMapping("/myPageMain")
//    public String myPageMain(HttpSession session, Model model) {
//        UserDTO loggedInUser = (UserDTO) session.getAttribute("loggedInUser");
//        // 로그인 유저 정보 로그 출력
//        if (loggedInUser == null) {
//            logger.info("로그인된 사용자 정보가 없습니다.");
//            return "redirect:/user/login";
//        } else {
//            logger.info("로그인된 사용자: {}", loggedInUser.toString());
//        }
//        String userId = loggedInUser.getUserId();
//        List<ImageInfo> latestImages = s3Service.getAllImagesSortedByLatest(userId);
//        model.addAttribute("latestImages", latestImages);
//        return "seoyun/myPageMain";
//    }
//
//    // 마이페이지 달력 페이지 이동
//    @GetMapping("/myPagePop")
//    public String myPagePop(HttpSession session, Model model) {
//        UserDTO loggedInUser = (UserDTO) session.getAttribute("loggedInUser");
//        // 로그인 유저 정보 로그 출력
//        if (loggedInUser == null) {
//            logger.info("로그인된 사용자 정보가 없습니다.");
//            return "redirect:/user/login";
//        } else {
//            logger.info("로그인된 사용자: {}", loggedInUser.toString());
//        }
//
//        String userId = loggedInUser.getUserId();
//        List<ImageInfo> latestImages = s3Service.getAllImagesSortedByLatest(userId);
//        model.addAttribute("latestImages", latestImages);
//        return "seoyun/myPagePop";
//    }
//
//    // 마이페이지 디테일 페이지 이동
//    @GetMapping("/myPageDetail")
//    public String myPageDetail(@RequestParam("timestamp") String timestamp, HttpSession session, Model model) {
//        UserDTO loggedInUser = (UserDTO) session.getAttribute("loggedInUser");
//        // 로그인 유저 정보 로그 출력
//        if (loggedInUser == null) {
//            logger.info("로그인된 사용자 정보가 없습니다.");
//            return "redirect:/user/login";
//        } else {
//            logger.info("로그인된 사용자: {}", loggedInUser.toString());
//        }
//
//        String userId = loggedInUser.getUserId();
//
//        // 해당 timestamp에 일치하는 영상 URL 가져오기
//        Optional<ImageInfo> video = s3Service.getVideoByTimestamp(userId, timestamp);
//        if (video.isPresent()) {
//            model.addAttribute("videoUrl", video.get().getUrl());
//            model.addAttribute("gptTitle", video.get().getGptTitle()); // GPT 제목 추가
//        } else {
//            model.addAttribute("videoUrl", "");
//            model.addAttribute("gptTitle", "제목 없음");
//        }
//
//        // 해당 timestamp에 일치하는 GPT 해결책 가져오기
//        Optional<String> gptResponse = s3Service.getGptResponseByVideoTimestamp(userId, timestamp);
//        if (gptResponse.isPresent()) {
//            model.addAttribute("gptResponse", gptResponse.get());
//        } else {
//            model.addAttribute("gptResponse", "해결책을 찾을 수 없습니다.");
//        }
//        return "seoyun/myPageDetail";
//    }
//
//    // 특정 날짜의 동영상을 가져오기 (Ajax)
//    @GetMapping("/getVideoByDate")
//    @ResponseBody
//    public ImageInfo getVideoByDate(@RequestParam("date") String date, HttpSession session) {
//        // 로그인된 사용자 정보를 세션에서 가져옴
//        UserDTO loggedInUser = (UserDTO) session.getAttribute("loggedInUser");
//
//        if (loggedInUser == null) {
//            logger.info("로그인된 사용자 정보가 없습니다.");
//            return null;  // 로그인되지 않은 경우 null 반환
//        } else {
//            logger.info("로그인된 사용자: {}", loggedInUser.toString());
//        }
//
//        String userId = loggedInUser.getUserId();
//        Optional<ImageInfo> video = s3Service.getVideoByDate(userId, date);
//        return video.orElse(null);
//    }
//
//
//    @GetMapping("/searchVideos")
//    @ResponseBody
//    public List<S3Service.ImageInfo> searchVideos(@RequestParam("keyword") String keyword, HttpSession session) {
//        UserDTO loggedInUser = (UserDTO) session.getAttribute("loggedInUser");
//        if (loggedInUser == null) {
//            return Collections.emptyList(); // 로그인되지 않은 경우 빈 리스트 반환
//        }
//        String userId = loggedInUser.getUserId();
//        return s3Service.searchImagesByKeyword(userId, keyword); // 키워드로 검색된 영상 반환
//    }
//}
@Controller
@RequestMapping("/myPage")
public class MyPageController {

    @Autowired
    private S3Service s3Service;
    private static final Logger logger = LoggerFactory.getLogger(MyPageController.class);

    // 마이페이지 이동
    @GetMapping("/myPageMain")
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

        // 사용자 이름과 ID를 모델에 추가
        model.addAttribute("userId", userId);
        model.addAttribute("userName", userName);

        List<ImageInfo> latestImages = s3Service.getAllImagesSortedByLatest(userId);
        model.addAttribute("latestImages", latestImages);
        return "seoyun/myPageMain";
    }


    // 마이페이지 달력 페이지 이동
    @GetMapping("/myPagePop")
    public String myPagePop(HttpSession session, Model model) {
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            return "redirect:/user/login";
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
            return "redirect:/user/login";
        }

        logger.info("로그인된 사용자: {}", loggedInUser.toString());

        List<ImageInfo> latestImages = s3Service.getAllImagesSortedByLatest(userId);
        model.addAttribute("latestImages", latestImages);
        return "seoyun/myPagePop";
    }

    // 마이페이지 디테일 페이지 이동
    @GetMapping("/myPageDetail")
    public String myPageDetail(@RequestParam("timestamp") String timestamp, HttpSession session, Model model) {
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            return "redirect:/user/login";
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
            return "redirect:/user/login";
        }

        logger.info("로그인된 사용자: {}", loggedInUser.toString());

        Optional<ImageInfo> video = s3Service.getVideoByTimestamp(userId, timestamp);
        if (video.isPresent()) {
            model.addAttribute("videoUrl", video.get().getUrl());
            model.addAttribute("gptTitle", video.get().getGptTitle());
        } else {
            model.addAttribute("videoUrl", "");
            model.addAttribute("gptTitle", "제목 없음");
        }
        // AI 동영상 정보 가져오기
        Optional<ImageInfo> aiVideo = s3Service.getAIVideoByTimestamp(userId, timestamp);
        if (aiVideo.isPresent()) {
            model.addAttribute("aiVideoUrl", aiVideo.get().getUrl());
            model.addAttribute("aiVideoKey", aiVideo.get().getKey());
        } else {
            model.addAttribute("aiVideoUrl", "");
            model.addAttribute("aiVideoKey", "");
        }

        Map<String, Object> gptResponse = s3Service.getGptResponseByVideoTimestamp(userId, timestamp);
        model.addAttribute("gptTitle", gptResponse.getOrDefault("gptTitle", "제목 없음"));
        model.addAttribute("gptSummary", gptResponse.getOrDefault("gptSummary", "요약 없음"));
        model.addAttribute("participants", gptResponse.getOrDefault("participants", Collections.emptyMap()));

        return "seoyun/myPageDetail";
    }

    // 특정 날짜의 동영상을 가져오기 (Ajax)
    @GetMapping("/getVideoByDate")
    @ResponseBody
    public ImageInfo getVideoByDate(@RequestParam("date") String date, HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            return null;
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
            return null;
        }

        logger.info("로그인된 사용자: {}", loggedInUser.toString());

        Optional<ImageInfo> video = s3Service.getVideoByDate(userId, date);
        return video.orElse(null);
    }

    @GetMapping("/searchVideos")
    @ResponseBody
    public List<S3Service.ImageInfo> searchVideos(@RequestParam("keyword") String keyword, HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return Collections.emptyList(); // 로그인되지 않은 경우 빈 리스트 반환
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
            return Collections.emptyList();
        }

        return s3Service.searchImagesByKeyword(userId, keyword); // 키워드로 검색된 영상 반환
    }
}

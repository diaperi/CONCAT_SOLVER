package concat.SolverWeb.myPage.myPageMain.controller;

import concat.SolverWeb.myPage.myPageMain.service.S3Service;
import concat.SolverWeb.myPage.myPageMain.service.S3Service.ImageInfo;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/myPage")
public class MyPageController {

    @Autowired
    private S3Service s3Service;
    private static final Logger logger = LoggerFactory.getLogger(MyPageController.class);


    // 마이페이지 이동
    @GetMapping("/myPageMain")
    public String myPageMain(HttpSession session, Model model) {
        UserDTO loggedInUser = (UserDTO) session.getAttribute("loggedInUser");
        // 로그인 유저 정보 로그 출력
        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            return "redirect:/user/login";
        } else {
            logger.info("로그인된 사용자: {}", loggedInUser.toString());
        }
        String userId = loggedInUser.getUserId();
        List<ImageInfo> latestImages = s3Service.getAllImagesSortedByLatest(userId);
        model.addAttribute("latestImages", latestImages);
        return "seoyun/myPageMain";
    }


    // 마이페이지 달력 페이지 이동
    @GetMapping("/myPagePop")
    public String myPagePop(HttpSession session, Model model) {
        UserDTO loggedInUser = (UserDTO) session.getAttribute("loggedInUser");
        // 로그인 유저 정보 로그 출력
        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            return "redirect:/user/login";
        } else {
            logger.info("로그인된 사용자: {}", loggedInUser.toString());
        }

        String userId = loggedInUser.getUserId();
        List<ImageInfo> latestImages = s3Service.getAllImagesSortedByLatest(userId);
        model.addAttribute("latestImages", latestImages);
        return "seoyun/myPagePop";
    }

    // 마이페이지 디테일 페이지 이동
    @GetMapping("/myPageDetail")
    public String myPageDetail(@RequestParam("timestamp") String timestamp, HttpSession session, Model model) {
        UserDTO loggedInUser = (UserDTO) session.getAttribute("loggedInUser");
        // 로그인 유저 정보 로그 출력
        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            return "redirect:/user/login";
        } else {
            logger.info("로그인된 사용자: {}", loggedInUser.toString());
        }

        String userId = loggedInUser.getUserId();
        Optional<ImageInfo> video = s3Service.getVideoByTimestamp(userId, timestamp);
        if (video.isPresent()) {
            model.addAttribute("videoUrl", video.get().getUrl());
        } else {
            model.addAttribute("videoUrl", "");
        }
        return "seoyun/myPageDetail";
    }

    // 특정 날짜의 동영상을 가져오기 (Ajax)
    @GetMapping("/getVideoByDate")
    @ResponseBody
    public ImageInfo getVideoByDate(@RequestParam("date") String date, HttpSession session) {
        // 로그인된 사용자 정보를 세션에서 가져옴
        UserDTO loggedInUser = (UserDTO) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            logger.info("로그인된 사용자 정보가 없습니다.");
            return null;  // 로그인되지 않은 경우 null 반환
        } else {
            logger.info("로그인된 사용자: {}", loggedInUser.toString());
        }

        String userId = loggedInUser.getUserId();
        Optional<ImageInfo> video = s3Service.getVideoByDate(userId, date);
        return video.orElse(null);
    }

}

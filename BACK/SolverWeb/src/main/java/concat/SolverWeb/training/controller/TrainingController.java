package concat.SolverWeb.training.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/train")
public class TrainingController {
    @GetMapping("/training")
    public String training() {return "hyeeun/training";}

    @GetMapping("/feedback")
    public String feedback() {
        return "hyeeun/feedback";
    }
}

//@Controller
//@RequestMapping("/train")
//public class TrainingController {
//
//    @Autowired
//    private S3Service s3Service;
//    private static final Logger logger = LoggerFactory.getLogger(concat.SolverWeb.myPage.myPageMain.controller.MyPageController.class);
//
//    // 마이페이지 달력 페이지 이동
//    @GetMapping("/myPagePop")
//    public String myPagePop(HttpSession session, Model model) {
//        Object loggedInUser = session.getAttribute("loggedInUser");
//
//        if (loggedInUser == null) {
//            logger.info("로그인된 사용자 정보가 없습니다.");
//            return "redirect:/user/login";
//        }
//
//        String userId;
//        if (loggedInUser instanceof UserDTO) {
//            UserDTO user = (UserDTO) loggedInUser;
//            userId = user.getUserId();
//        } else if (loggedInUser instanceof SnsUserDTO) {
//            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
//            userId = snsUser.getProviderId();
//        } else {
//            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
//            return "redirect:/user/login";
//        }
//
//        logger.info("로그인된 사용자: {}", loggedInUser.toString());
//
//        List<S3Service.ImageInfo> latestImages = s3Service.getAllImagesSortedByLatest(userId);
//        model.addAttribute("latestImages", latestImages);
//        return "seoyun/myPagePop";
//    }
//
//    // 마이페이지 디테일 페이지 이동
//    @GetMapping("/myPageDetail")
//    public String myPageDetail(@RequestParam("timestamp") String timestamp, HttpSession session, Model model) {
//        Object loggedInUser = session.getAttribute("loggedInUser");
//
//        if (loggedInUser == null) {
//            logger.info("로그인된 사용자 정보가 없습니다.");
//            return "redirect:/user/login";
//        }
//
//        String userId;
//        if (loggedInUser instanceof UserDTO) {
//            UserDTO user = (UserDTO) loggedInUser;
//            userId = user.getUserId();
//        } else if (loggedInUser instanceof SnsUserDTO) {
//            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
//            userId = snsUser.getProviderId();
//        } else {
//            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
//            return "redirect:/user/login";
//        }
//
//        logger.info("로그인된 사용자: {}", loggedInUser.toString());
//
//        Optional<S3Service.ImageInfo> video = s3Service.getVideoByTimestamp(userId, timestamp);
//        if (video.isPresent()) {
//            model.addAttribute("videoUrl", video.get().getUrl());
//            model.addAttribute("gptTitle", video.get().getGptTitle());
//        } else {
//            model.addAttribute("videoUrl", "");
//            model.addAttribute("gptTitle", "제목 없음");
//        }
//
//        Optional<String> gptResponse = s3Service.getGptResponseByVideoTimestamp(userId, timestamp);
//        if (gptResponse.isPresent()) {
//            model.addAttribute("gptResponse", gptResponse.get());
//        } else {
//            model.addAttribute("gptResponse", "해결책을 찾을 수 없습니다.");
//        }
//        return "hyeeun/feedback";
//    }
//
//    // 특정 날짜의 동영상을 가져오기 (Ajax)
//    @GetMapping("/getVideoByDate")
//    @ResponseBody
//    public S3Service.ImageInfo getVideoByDate(@RequestParam("date") String date, HttpSession session) {
//        Object loggedInUser = session.getAttribute("loggedInUser");
//
//        if (loggedInUser == null) {
//            logger.info("로그인된 사용자 정보가 없습니다.");
//            return null;
//        }
//
//        String userId;
//        if (loggedInUser instanceof UserDTO) {
//            UserDTO user = (UserDTO) loggedInUser;
//            userId = user.getUserId();
//        } else if (loggedInUser instanceof SnsUserDTO) {
//            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
//            userId = snsUser.getProviderId();
//        } else {
//            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
//            return null;
//        }
//
//        logger.info("로그인된 사용자: {}", loggedInUser.toString());
//
//        Optional<S3Service.ImageInfo> video = s3Service.getVideoByDate(userId, date);
//        return video.orElse(null);
//    }
//
//    @GetMapping("/searchVideos")
//    @ResponseBody
//    public List<S3Service.ImageInfo> searchVideos(@RequestParam("keyword") String keyword, HttpSession session) {
//        Object loggedInUser = session.getAttribute("loggedInUser");
//
//        if (loggedInUser == null) {
//            return Collections.emptyList(); // 로그인되지 않은 경우 빈 리스트 반환
//        }
//
//        String userId;
//        if (loggedInUser instanceof UserDTO) {
//            UserDTO user = (UserDTO) loggedInUser;
//            userId = user.getUserId();
//        } else if (loggedInUser instanceof SnsUserDTO) {
//            SnsUserDTO snsUser = (SnsUserDTO) loggedInUser;
//            userId = snsUser.getProviderId();
//        } else {
//            logger.warn("예상치 못한 사용자 유형입니다: {}", loggedInUser.getClass());
//            return Collections.emptyList();
//        }
//
//        return s3Service.searchImagesByKeyword(userId, keyword); // 키워드로 검색된 영상 반환
//    }
//}

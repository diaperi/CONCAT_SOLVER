package concat.SolverWeb.myPage.myPageMain.controller;

import concat.SolverWeb.myPage.myPageMain.service.S3Service;
import concat.SolverWeb.myPage.myPageMain.service.S3Service.ImageInfo;
import concat.SolverWeb.myPage.trashCs.service.TrashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/myPage")
public class MyPageController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private TrashService trashService;

    // 마이페이지 이동
    @GetMapping("/myPageMain")
    public String myPageMain(Model model) {
        List<ImageInfo> latestImages = s3Service.getAllImagesSortedByLatest();
        model.addAttribute("latestImages", latestImages);
        return "seoyun/myPageMain";
    }

    // 마이페이지 달력 페이지 이동
    @GetMapping("/myPagePop")
    public String myPagePop(Model model) {
        List<ImageInfo> latestImages = s3Service.getAllImagesSortedByLatest();
        model.addAttribute("latestImages", latestImages);
        return "seoyun/myPagePop";
    }

    // 마이페이지 디테일 페이지 이동
    @GetMapping("/myPageDetail")
    public String myPageDetail(@RequestParam("timestamp") String timestamp, Model model) {
        Optional<ImageInfo> video = s3Service.getVideoByTimestamp(timestamp);
        if (video.isPresent()) {
            model.addAttribute("videoUrl", video.get().getUrl());
            System.out.println("Video URL: " + video.get().getUrl()); // 디버그용 로그 출력
        } else {
            model.addAttribute("videoUrl", "");
        }
        return "seoyun/myPageDetail";
    }


    // 특정 날짜의 동영상을 가져오기 (Ajax)
    @GetMapping("/getVideoByDate")
    @ResponseBody
    public ImageInfo getVideoByDate(@RequestParam("date") String date) {
        Optional<ImageInfo> video = s3Service.getVideoByDate(date);
        return video.orElse(null);
    }

//    // 파일 삭제 요청 처리 (AJAX)
//    @PostMapping("/delete-file")
//    @ResponseBody
//    public Map<String, Object> deleteFile(@RequestBody Map<String, String> requestBody) {
//        String fileName = requestBody.get("fileName");
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            trashService.deleteFile(fileName);
//            response.put("success", true);
//        } catch (Exception e) {
//            response.put("success", false);
//            response.put("message", e.getMessage());
//        }
//
//        return response;
//    }

}

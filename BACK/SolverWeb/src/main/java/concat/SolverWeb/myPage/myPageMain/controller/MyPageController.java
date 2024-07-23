package concat.SolverWeb.myPage.myPageMain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/myPage")
public class MyPageController {

    // 마이페이지 이동
    @GetMapping("/myPageMain")
    public String myPageMain() {
        return "seoyun/myPageMain";
    }

    // 마이페이지 달력 페이지 이동
    @GetMapping("/myPagePop")
    public String myPagePop() {
        return "seoyun/myPagePop";
    }

    // 마이페이지 디테일 페이지 이동
    @GetMapping("/myPageDetail")
    public String myPageDetail() {
        return "seoyun/myPageDetail";
    }


}

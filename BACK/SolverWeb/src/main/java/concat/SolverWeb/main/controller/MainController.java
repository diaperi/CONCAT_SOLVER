package concat.SolverWeb.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/main")
public class MainController {

    //  메인 페이지 이동
    @GetMapping("/mainPage")
    public String mainPage() {
        return "hyeeun/mainPage";
    }
}

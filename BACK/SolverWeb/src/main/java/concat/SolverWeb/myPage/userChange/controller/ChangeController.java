package concat.SolverWeb.myPage.userChange.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChangeController {
    @GetMapping("/member")
    public String home() {
        return "hyeeun/userchange/changeindex";
    }
}

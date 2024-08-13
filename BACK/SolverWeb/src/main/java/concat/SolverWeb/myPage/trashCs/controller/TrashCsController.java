package concat.SolverWeb.myPage.trashCs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/myPage")
public class TrashCsController {

    // 마이페이지 고객센터 페이지 이동
    @GetMapping("/cs")
    public String cs() {
        return "yuna/cs";
    }

    // 마이페이지 휴지통 페이지 이동
    @GetMapping("/trash")
    public String trash() {
        return "yuna/trash";
    }
}
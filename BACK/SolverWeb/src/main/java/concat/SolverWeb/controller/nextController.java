package concat.SolverWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class nextController {

    @GetMapping("/checkInputs")
    public String checkInputs(){
        return "yoonseo/sign";
    }
}

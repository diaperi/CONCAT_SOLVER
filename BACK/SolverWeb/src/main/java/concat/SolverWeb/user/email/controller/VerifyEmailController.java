package concat.SolverWeb.user.email.controller;

import concat.SolverWeb.user.email.service.VerifyEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class VerifyEmailController {

    private final VerifyEmailService verifyEmailService;

    @Autowired
    public VerifyEmailController(VerifyEmailService verifyEmailService) {
        this.verifyEmailService = verifyEmailService;
    }

    @GetMapping("/verify/{userNo}")
    public String verifyEmail(@PathVariable Integer userNo) {
        boolean isVerified = verifyEmailService.verifyEmail(userNo);
        if (isVerified) {
            return "yuna/verifySuccess";
        } else {
            return "yuna/verifyFailed";
        }
    }

}

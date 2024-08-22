package concat.SolverWeb.user.email.controller;

import concat.SolverWeb.user.email.service.VerifyEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @ResponseBody
    public ResponseEntity<String> verifyEmail(@PathVariable Integer userNo) {
        boolean isVerified = verifyEmailService.verifyEmail(userNo);
        if (isVerified) {
            return ResponseEntity.ok("인증 완료");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 실패");
        }
    }
}

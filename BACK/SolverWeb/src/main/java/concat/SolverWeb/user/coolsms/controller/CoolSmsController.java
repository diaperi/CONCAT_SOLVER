package concat.SolverWeb.user.coolsms.controller;

import concat.SolverWeb.user.coolsms.service.phone.CoolSmsService;
import concat.SolverWeb.user.coolsms.service.phone.PhoneVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sms")
public class CoolSmsController {

    private final CoolSmsService coolSmsService;
    private final PhoneVerificationService phoneVerificationService;

    public CoolSmsController(CoolSmsService coolSmsService, PhoneVerificationService phoneVerificationService) {
        this.coolSmsService = coolSmsService;
        this.phoneVerificationService = phoneVerificationService;
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/sendVerificationCode")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String userId = request.get("userId"); // userId를 받습니다.
        String phoneNumber = request.get("phoneNumber");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("전화번호를 입력하세요."));
        }

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("userId를 입력하세요."));
        }

        try {
            // 전화번호 중복 확인
            if (phoneVerificationService.isPhoneNumberDuplicate(phoneNumber)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse("이미 등록된 전화번호입니다."));
            }

            // 인증 코드 전송
            boolean success = phoneVerificationService.sendVerificationCode(phoneNumber);

            if (success) {
                // 전화번호 저장
                boolean isSaved = phoneVerificationService.saveOrUpdateUserPhone(userId, phoneNumber);

                if (isSaved) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "인증 코드 전송, 전화번호 저장 성공");
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createErrorResponse("전화번호 저장 실패"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("인증 코드 전송 실패"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("인증 코드 전송 중 오류 발생"));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/verifyCode")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String inputCode = request.get("verificationCode");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("전화번호를 입력하세요."));
        }

        if (inputCode == null || inputCode.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("인증 코드를 입력하세요."));
        }

        boolean isVerified = phoneVerificationService.verifyCode(phoneNumber, inputCode);

        if (isVerified) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "인증 성공");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("인증 코드가 잘못되었습니다."));
        }
    }

    // 오류 메시지 반환을 위한 메서드
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    @GetMapping("/checkVerificationStatus")
    public ResponseEntity<Map<String, String>> checkVerificationStatus(@RequestParam String userId) {
        Map<String, String> response = new HashMap<>();
        boolean isVerified = phoneVerificationService.checkVerificationStatus(userId);

        if (isVerified) {
            response.put("status", "Y");
        } else {
            response.put("status", "N");
        }

        return ResponseEntity.ok(response);
    }
}



package concat.SolverWeb.user.email.service.verify;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class VerifyCodeService {

    private static final int CODE_LEN = 6;
    private static final Map<String, String> VERIFY_CODE = new HashMap<>();

    // 인증 코드 생성
    public String generateVerifyCode() {
        Random random = new Random();
        return String.format("%0" + CODE_LEN + "d", random.nextInt((int) Math.pow(10, CODE_LEN)));
    }

    // 인증 코드 저장
    public void storeVerifyCode(String email, String code) {
        VERIFY_CODE.put(email, code);
    }

    // 인증 코드 확인
    public boolean checkVerifyCode(String email, String code) {
        String storedCode = VERIFY_CODE.get(email);
        return storedCode != null && storedCode.equals(code);
    }
}

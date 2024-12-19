package concat.SolverWeb.user.coolsms.service.phone;

import io.github.cdimascio.dotenv.Dotenv;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import org.springframework.stereotype.Service;

@Service
public class CoolSmsService {

    private final String apiKey;
    private final String apiSecret;
    private final String senderNumber;

    // 생성자에서 .env 파일 로드
    public CoolSmsService() {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("COOLSMS_ACCESS_KEY");
        this.apiSecret = dotenv.get("COOLSMS_SECRET_ACCESS_KEY");
        this.senderNumber = dotenv.get("COOLSMS_NUMBER");
    }

    // 인증 코드 전송
    public boolean sendCertificationCode(String phoneNumber, String code) {
        try {
            // CoolSMS API 서비스 초기화
            DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");

            // 인증 메시지 구성
            Message message = new Message();
            message.setFrom(senderNumber);  // 발신자
            message.setTo(phoneNumber);     // 수신자
            message.setText("요청하신 SOLVER 인증번호는 " + code + "입니다.");

            // 메시지 전송
            messageService.send(message); // 메시지 전송

            return true; // 전송 성공
        } catch (NurigoMessageNotReceivedException exception) {
            // 발송 실패한 메시지 목록 출력
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
            return false;
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            return false;
        }
    }
}


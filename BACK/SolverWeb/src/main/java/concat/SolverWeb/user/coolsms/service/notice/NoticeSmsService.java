package concat.SolverWeb.user.coolsms.service.notice;

import io.github.cdimascio.dotenv.Dotenv;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NoticeSmsService {

    private final String apiKey;
    private final String apiSecret;
    private final String senderNumber;

    private static final Logger logger = LoggerFactory.getLogger(NoticeSmsService.class);

    // 생성자에서 .env 파일 로드
    public NoticeSmsService() {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("COOLSMS_ACCESS_KEY");
        this.apiSecret = dotenv.get("COOLSMS_SECRET_ACCESS_KEY");
        this.senderNumber = dotenv.get("COOLSMS_NUMBER");  // 발신자 번호
    }

    // 해결책 알림 문자 전송
    public boolean checkNewFileInS3(String phoneNumber) {
        try {
            // CoolSMS API 서비스 초기화
            DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");

            // 인증 메시지 구성
            Message message = new Message();
            message.setFrom(senderNumber);  // 발신자
            message.setTo(phoneNumber);     // 수신자
            message.setText("SOLVER 해결책이 업로드 되었습니다.");

            // 메시지 전송
            messageService.send(message); // 메시지 전송

            logger.info("문자 전송 성공: {}", phoneNumber);
            return true;
        } catch (NurigoMessageNotReceivedException exception) {
            // 발송 실패한 메시지 로그
            logger.error("전송 실패한 메시지: {}", exception.getFailedMessageList(), exception);
            return false;
        } catch (Exception exception) {
            logger.error("문자 전송 중 예외 발생: {}", exception.getMessage(), exception);
            return false;
        }
    }
}


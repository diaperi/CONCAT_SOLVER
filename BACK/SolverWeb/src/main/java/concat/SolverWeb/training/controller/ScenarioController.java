// 주석 처리 - 주파수 스펙트로그램 관련 코드
package concat.SolverWeb.training.controller;

import concat.SolverWeb.training.service.GPTService;
import concat.SolverWeb.training.service.STTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/scenario")
public class ScenarioController {

    @Autowired
    private STTService sttService;

    @Autowired
    private GPTService gptService;

    private static final Logger logger = LoggerFactory.getLogger(ScenarioController.class);

    @PostMapping("/sc")
    public ResponseEntity<?> handleAudioUpload(@RequestParam("audioFile") MultipartFile audioFile) {
        synchronized (this) {
            try {
                // 1. 녹음 파일을 STT로 변환
                String sttResult = sttService.uploadAudioAndGetText(audioFile);

                // 2. 고정된 스펙트로그램 파일 이름 설정
//                String outputDirectory = "D:/CONCAT/CONCAT_SOLVER/BACK/SolverWeb/src/main/resources/static/media/";
//                String melSpectrogramPath = Paths.get(outputDirectory, "fft_spectrogram.png").toString();

                /* 스펙트로그램 생성 및 저장 - 주석 처리 시작 */
                // 3. 스펙트로그램 생성 및 저장
                // sttService.generateMelSpectrogram(audioFile.getBytes(), melSpectrogramPath);

                // 4. 이미지 생성 완료 여부 확인
                // File generatedImage = new File(melSpectrogramPath);
                // if (generatedImage.exists()) {

                // 5. 웹 경로 설정
                // String webMelSpectrogramPath = "/media/fft_spectrogram.png?&=" + System.currentTimeMillis();

                // 6. 피드백 생성 (스펙트로그램 이미지 관련 코드 제외)
                String feedback = gptService.getFeedback(sttResult, null);

                // 7. 응답으로 반환할 데이터 구성
                Map<String, Object> response = new HashMap<>();
                response.put("sttResult", sttResult);
                response.put("melSpectrogram", ""); // 스펙트로그램 이미지 경로 대신 빈 문자열 할당
                response.put("feedback", feedback);

                // 8. Cache-Control 헤더 추가 (캐시 무효화)
                return ResponseEntity.ok()
                        .cacheControl(CacheControl.noCache().mustRevalidate())
                        .body(response);

            } catch (Exception e) {
                logger.error("파일 처리 중 오류 발생: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "파일 처리 중 오류가 발생했습니다."));
            }
        }
    }

    @GetMapping("/getScenario")
    public ResponseEntity<?> getScenario() {
        try {
            // GPT API로 갈등 상황과 대화문 받아오기
            String conflictScenario = gptService.getConflictScenario();
            String conversation = gptService.getGPTConversation(conflictScenario);

            Map<String, String> response = new HashMap<>();
            response.put("conflictScenario", conflictScenario);
            response.put("conversation", conversation);

            return ResponseEntity.ok(response);
        } catch (WebClientException e) {
            logger.error("API 호출 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "API 호출 중 오류가 발생했습니다."));
        } catch (RuntimeException e) {
            logger.error("시나리오 가져오기 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "시나리오를 가져오는 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/sc")
    public String getScenarioPage(Model model) {
        // 필요한 데이터 처리 후 시나리오 페이지 반환
        return "hyeeun/trains/scenario";  // 시나리오 페이지에 해당하는 템플릿 이름
    }
}


package concat.SolverWeb.training.controller;

import concat.SolverWeb.training.service.STTService;
import concat.SolverWeb.training.service.GPTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientException;

import java.io.IOException;
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

    // **************************** 캐시 무효화 ******************************** //
    @PostMapping("/sc")
    public ResponseEntity<?> handleAudioUpload(@RequestParam("audioFile") MultipartFile audioFile) {
        try {
            String sttResult = sttService.uploadAudioAndGetText(audioFile);

            // 절대 경로 설정 (파일을 서버의 특정 경로에 저장)
            String outputDirectory = "D:/CONCAT/CONCAT_SOLVER/BACK/SolverWeb/src/main/resources/static/media/";
            String absoluteMelSpectrogramPath = outputDirectory + "fft_spectrogram.png";
            sttService.generateMelSpectrogram(audioFile.getBytes(), absoluteMelSpectrogramPath);

            // 클라이언트가 접근할 수 있는 웹 경로
            String webMelSpectrogramPath = "/media/fft_spectrogram.png";

            String feedback = gptService.getFeedback(sttResult, absoluteMelSpectrogramPath);

            // 응답으로 반환할 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("sttResult", sttResult);
            response.put("melSpectrogram", webMelSpectrogramPath);
            response.put("feedback", feedback);

            // Cache-Control 헤더 추가 (캐시 무효화)
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache().mustRevalidate()) // 캐시를 사용하지 않도록 설정
                    .body(response);
        } catch (Exception e) {
            logger.error("파일 처리 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "파일 처리 중 오류가 발생했습니다."));
        }
    }


    // *********************************************************************** //
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





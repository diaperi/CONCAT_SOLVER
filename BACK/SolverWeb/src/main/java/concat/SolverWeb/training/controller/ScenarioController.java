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

import java.io.File;
import java.nio.file.Paths;
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
                String outputDirectory = "D:/CONCAT/CONCAT_SOLVER/BACK/SolverWeb/src/main/resources/static/media/";
                String melSpectrogramPath = Paths.get(outputDirectory, "fft_spectrogram.png").toString();

                // 3. 스펙트로그램 생성 및 저장
                sttService.generateMelSpectrogram(audioFile.getBytes(), melSpectrogramPath);

                // 4. 이미지 생성 완료 여부 확인
                File generatedImage = new File(melSpectrogramPath);
                if (generatedImage.exists()) {
                    // 5. 웹 경로 설정
                    String webMelSpectrogramPath = "/media/fft_spectrogram.png?&=" + System.currentTimeMillis();

                    // 6. 피드백 생성 (이미지 생성 완료 후)
                    String feedback = gptService.getFeedback(sttResult, melSpectrogramPath);

                    // 7. 응답으로 반환할 데이터 구성
                    Map<String, Object> response = new HashMap<>();
                    response.put("sttResult", sttResult);
                    response.put("melSpectrogram", webMelSpectrogramPath);
                    response.put("feedback", feedback);

                    // 8. Cache-Control 헤더 추가 (캐시 무효화)
                    return ResponseEntity.ok()
                            .cacheControl(CacheControl.noCache().mustRevalidate())
                            .body(response);
                } else {
                    // 이미지 생성 실패 처리
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Collections.singletonMap("error", "스펙트로그램 이미지 생성에 실패했습니다."));
                }
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


//package concat.SolverWeb.training.controller;
//
//import concat.SolverWeb.training.service.GPTService;
//import concat.SolverWeb.training.service.STTService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.CacheControl;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.nio.file.Paths;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//
//@Controller
//@RequestMapping("/scenario")
//public class ScenarioController {
//
//    @Autowired
//    private STTService sttService;
//
//    @Autowired
//    private GPTService gptService;
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;  // WebSocket 메시지를 전송하기 위한 템플릿
//
//    private static final Logger logger = LoggerFactory.getLogger(ScenarioController.class);
//
//    @PostMapping("/sc")
//    public ResponseEntity<?> handleAudioUpload(@RequestParam("audioFile") MultipartFile audioFile) {
//        try {
//            // 1. 녹음 파일을 STT로 변환
//            String sttResult = sttService.uploadAudioAndGetText(audioFile);
//
//            // 2. 고정된 스펙트로그램 파일 이름 설정
//            String outputDirectory = "D:/CONCAT/CONCAT_SOLVER/BACK/SolverWeb/src/main/resources/static/media/";
//            String melSpectrogramPath = Paths.get(outputDirectory, "fft_spectrogram.png").toString();
//
//            // 3. 스펙트로그램 생성 및 저장
//            sttService.generateMelSpectrogram(audioFile.getBytes(), melSpectrogramPath);
//
//            // 4. 이미지 생성 완료 여부 확인
//            File generatedImage = new File(melSpectrogramPath);
//            if (generatedImage.exists()) {
//                // 5. 웹 경로 설정
//                String webMelSpectrogramPath = "/media/fft_spectrogram.png?t=" + System.currentTimeMillis();
//
//                // 6. 피드백 생성 (이미지 생성 완료 후)
//                String feedback = gptService.getFeedback(sttResult, melSpectrogramPath);
//
//                // 7. WebSocket을 통해 클라이언트로 메시지 전송
//                Map<String, Object> response = new HashMap<>();
//                response.put("sttResult", sttResult);
//                response.put("melSpectrogram", webMelSpectrogramPath);
//                response.put("feedback", feedback);
//
//                messagingTemplate.convertAndSend("/topic/audioResponse", response);  // WebSocket 메시지 전송
//
//                // 8. 응답 반환
//                return ResponseEntity.ok()
//                        .cacheControl(CacheControl.noCache().mustRevalidate())
//                        .body(response);
//            } else {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(Collections.singletonMap("error", "스펙트로그램 이미지 생성에 실패했습니다."));
//            }
//        } catch (Exception e) {
//            logger.error("파일 처리 중 오류 발생: ", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "파일 처리 중 오류가 발생했습니다."));
//        }
//    }
//
//    @GetMapping("/getScenario")
//    public ResponseEntity<?> getScenario() {
//        try {
//            // GPT API로 갈등 상황과 대화문 받아오기
//            String conflictScenario = gptService.getConflictScenario();
//            String conversation = gptService.getGPTConversation(conflictScenario);
//
//            Map<String, String> response = new HashMap<>();
//            response.put("conflictScenario", conflictScenario);
//            response.put("conversation", conversation);
//
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            logger.error("시나리오 가져오기 중 오류 발생: ", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "시나리오를 가져오는 중 오류가 발생했습니다."));
//        }
//    }
//
//    @GetMapping("/sc")
//    public String getScenarioPage(Model model) {
//        // 필요한 데이터 처리 후 시나리오 페이지 반환
//        return "hyeeun/trains/scenario";  // 시나리오 페이지에 해당하는 템플릿 이름
//    }
//}


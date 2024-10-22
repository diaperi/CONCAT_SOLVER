// 주석 처리 = 타임스탬프로 파일 저장
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

    // **************************** 캐시 무효화 ******************************** //
    @PostMapping("/sc")
    public ResponseEntity<?> handleAudioUpload(@RequestParam("audioFile") MultipartFile audioFile) {
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
                String webMelSpectrogramPath = "/media/fft_spectrogram.png?t=" + System.currentTimeMillis();

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

    // **************************** 이미지 여러개로 저장 ******************************** //
//    @PostMapping("/sc")
//    public ResponseEntity<?> handleAudioUpload(@RequestParam("audioFile") MultipartFile audioFile) {
//        try {
//            // 1. 녹음 파일을 STT로 변환
//            String sttResult = sttService.uploadAudioAndGetText(audioFile);
//
//            // 2. 타임스탬프를 사용해 고유한 파일 이름 생성
//            String outputDirectory = "D:/CONCAT/CONCAT_SOLVER/BACK/SolverWeb/src/main/resources/static/media/";
//            String timestamp = String.valueOf(System.currentTimeMillis());
//            String melSpectrogramPath = Paths.get(outputDirectory, "fft_spectrogram_" + timestamp + ".png").toString();
//
//            // 3. 스펙트로그램 생성 및 저장
//            String generatedSpectrogramPath = sttService.generateMelSpectrogram(audioFile.getBytes(), melSpectrogramPath);
//
//            // 4. GPT 서비스에 새로운 파일 경로 전달
//            String feedback = gptService.getFeedback(sttResult, generatedSpectrogramPath);
//
//            // 5. 웹 경로도 타임스탬프를 포함하여 클라이언트에 반환
//            String webMelSpectrogramPath = "/media/fft_spectrogram_" + timestamp + ".png";
//
//            // 응답으로 반환할 데이터 구성
//            Map<String, Object> response = new HashMap<>();
//            response.put("sttResult", sttResult);
//            response.put("melSpectrogram", webMelSpectrogramPath);
//            response.put("feedback", feedback);
//
//            // Cache-Control 헤더 추가 (캐시 무효화)
//            return ResponseEntity.ok()
//                    .cacheControl(CacheControl.noCache().mustRevalidate()) // 캐시를 사용하지 않도록 설정
//                    .body(response);
//        } catch (Exception e) {
//            logger.error("파일 처리 중 오류 발생: ", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "파일 처리 중 오류가 발생했습니다."));
//        }
//    }
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
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.reactive.function.client.WebClientException;
//
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
//    private static final Logger logger = LoggerFactory.getLogger(ScenarioController.class);
//
//    // **************************** 오디오 파일 처리 및 피드백 생성 ******************************** //
//    @PostMapping("/sc")
//    public ResponseEntity<?> handleAudioUpload(@RequestParam("audioFile") MultipartFile audioFile) {
//        try {
//            // 1. 오디오 파일을 STT로 변환 (오디오를 텍스트로 변환)
//            String sttResult = sttService.uploadAudioAndGetText(audioFile);
//
//            // 2. 타임스탬프 기반으로 고유한 스펙트로그램 파일 이름 생성
//            String timestamp = String.valueOf(System.currentTimeMillis());
//            String melSpectrogramPath = Paths.get("D:/CONCAT/CONCAT_SOLVER/BACK/SolverWeb/src/main/resources/static/media/", "fft_spectrogram_" + timestamp + ".png").toString();
//
//            // 3. 스펙트로그램 생성 및 저장
//            String generatedSpectrogramPath = sttService.generateMelSpectrogram(audioFile.getBytes(), melSpectrogramPath);
//
//            // 4. GPT 서비스로 텍스트 및 스펙트로그램 파일 경로 전달하여 피드백 생성
//            String feedback = gptService.getFeedback(sttResult, generatedSpectrogramPath);
//
//            // 5. 웹 경로 반환 (클라이언트 측에서 접근 가능한 URL)
//            String webMelSpectrogramPath = "/media/fft_spectrogram_" + timestamp + ".png";
//
//            // 응답으로 반환할 데이터 구성
//            Map<String, Object> response = new HashMap<>();
//            response.put("sttResult", sttResult);
//            response.put("melSpectrogram", webMelSpectrogramPath);
//            response.put("feedback", feedback);
//
//            // Cache-Control 헤더 추가 (캐시 무효화 설정)
//            return ResponseEntity.ok()
//                    .cacheControl(CacheControl.noCache().mustRevalidate())
//                    .body(response);
//        } catch (Exception e) {
//            logger.error("오디오 파일 처리 중 오류 발생: ", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "오디오 파일 처리 중 오류가 발생했습니다."));
//        }
//    }
//
//    // **************************** 갈등 시나리오 생성 및 대화 불러오기 ******************************** //
//    @GetMapping("/getScenario")
//    public ResponseEntity<?> getScenario() {
//        try {
//            // GPT 서비스를 이용하여 갈등 시나리오 및 대화 가져오기
//            String conflictScenario = gptService.getConflictScenario();
//            String conversation = gptService.getGPTConversation(conflictScenario);
//
//            // 응답으로 반환할 데이터 구성
//            Map<String, String> response = new HashMap<>();
//            response.put("conflictScenario", conflictScenario);
//            response.put("conversation", conversation);
//
//            return ResponseEntity.ok(response);
//        } catch (WebClientException e) {
//            logger.error("API 호출 중 오류 발생: ", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "API 호출 중 오류가 발생했습니다."));
//        } catch (RuntimeException e) {
//            logger.error("시나리오 가져오기 중 오류 발생: ", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", "시나리오를 가져오는 중 오류가 발생했습니다."));
//        }
//    }
//
//    // **************************** 시나리오 페이지 반환 ******************************** //
//    @GetMapping("/sc")
//    public String getScenarioPage(Model model) {
//        return "hyeeun/trains/scenario";  // 시나리오 페이지의 템플릿 이름
//    }
//}


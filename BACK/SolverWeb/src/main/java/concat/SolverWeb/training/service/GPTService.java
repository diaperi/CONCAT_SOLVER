package concat.SolverWeb.training.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GPTService {

    private static final Logger logger = LoggerFactory.getLogger(GPTService.class);

    // Dotenv을 사용하여 .env 파일에서 API 키 로드
    private final String apiKey;

    // 기본 URL
    private final String baseUrl = "https://api.openai.com/v1/chat/completions";

    public GPTService() {
        Dotenv dotenv = Dotenv.load(); // .env 파일 로드
        this.apiKey = dotenv.get("OPENAI_API_KEY"); // API 키 가져오기
    }

    public String getConflictScenario() {
        String prompt = "훈련을 위한 한 가지 갈등 시나리오를 간단하게 한국어로 제공해주세요. 형식은 시나리오 : ~ 입니다.";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", new Object[]{new HashMap<String, String>() {{
            put("role", "user");
            put("content", prompt);
        }}});
        requestBody.put("max_tokens", 300);
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            logger.info("GPT API 요청 본문: {}", requestBody);
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, requestEntity, Map.class);

            // API 응답에서 내용 추출
            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    return content.trim();  // 갈등 시나리오의 내용 반환
                } else {
                    logger.error("응답의 선택 항목이 비어 있습니다.");
                    throw new RuntimeException("응답의 선택 항목이 비어 있습니다.");
                }
            } else {
                logger.error("응답 본문이 비어 있습니다.");
                throw new RuntimeException("응답 본문이 비어 있습니다.");
            }
        } catch (RestClientException e) {
            logger.error("GPT API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("GPT API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public String getGPTConversation(String conflictScenario) {
        // 갈등 시나리오에 대한 첫 대화를 생성하기 위한 프롬프트
        String prompt = String.format("%s에 대해 한명의 역할을 맡아 첫 대화를 시작하세요. 형식은 역할 이름: ~입니다.", conflictScenario);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", new Object[]{new HashMap<String, String>() {{
            put("role", "user");
            put("content", prompt);
        }}});
        requestBody.put("max_tokens", 300);
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            logger.info("GPT API 요청 본문: {}", requestBody);
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, requestEntity, Map.class);

            // API 응답에서 내용 추출
            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    return content.trim();  // 첫 대화 내용 반환
                } else {
                    logger.error("응답의 선택 항목이 비어 있습니다.");
                    throw new RuntimeException("응답의 선택 항목이 비어 있습니다.");
                }
            } else {
                logger.error("응답 본문이 비어 있습니다.");
                throw new RuntimeException("응답 본문이 비어 있습니다.");
            }
        } catch (RestClientException e) {
            logger.error("GPT API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("GPT API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 이미지 파일을 Base64로 인코딩하는 메서드
    public String encodeImageToBase64(String absoluteImagePath) {
        try {
            // 절대 경로에서 파일을 읽어오기
            File file = new File(absoluteImagePath);

            // 파일 존재 여부 확인
            if (!file.exists()) {
                throw new RuntimeException("파일이 존재하지 않습니다: " + absoluteImagePath);
            }

            // 파일을 Base64로 인코딩
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            logger.error("이미지 인코딩 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("이미지 인코딩 중 오류가 발생했습니다: " + absoluteImagePath, e);
        }
    }

    // FFT 스펙트로그램 이미지에서 특징을 추출하는 메서드
    public String extractFeaturesFromImage(String melSpectrogramPath) {
        try {
            // 이미지 파일을 읽어오기
            BufferedImage image = ImageIO.read(new File(melSpectrogramPath));
            int width = image.getWidth();
            int height = image.getHeight();

            // 주파수 대역별 에너지를 계산하는 변수
            double totalEnergy = 0;
            double[] frequencyBands = new double[10]; // 10개의 주파수 대역

            // 이미지 픽셀을 순회하며 에너지를 계산
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = new Color(image.getRGB(x, y));
                    int brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3; // 밝기 계산
                    totalEnergy += brightness;

                    // 주파수 대역별로 에너지를 누적
                    int bandIndex = (int) ((double) y / height * frequencyBands.length);
                    if (bandIndex < frequencyBands.length) {
                        frequencyBands[bandIndex] += brightness;
                    }
                }
            }

            // 총 에너지와 주파수 대역별 에너지를 문자열로 생성
            StringBuilder featuresBuilder = new StringBuilder();
            featuresBuilder.append("총 에너지: ").append(totalEnergy).append("\n");
            for (int i = 0; i < frequencyBands.length; i++) {
                featuresBuilder.append("주파수 대역 ").append(i + 1).append(": ").append(frequencyBands[i]).append("\n");
            }

            return featuresBuilder.toString();
        } catch (IOException e) {
            logger.error("특징 추출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("특징 추출 중 오류가 발생했습니다: " + melSpectrogramPath, e);
        }
    }

    // STT 결과와 FFT 스펙트로그램 이미지에 대한 피드백을 생성하는 메서드
    public String getFeedback(String sttResult, String melSpectrogramPath) {
        // 이미지 경로를 Base64로 인코딩
        String melSpectrogramBase64 = encodeImageToBase64(melSpectrogramPath);

        // 이미지에서 특징 추출
        String features = extractFeaturesFromImage(melSpectrogramPath);

        // 이미지 크기에 따라 max_tokens 계산
        int maxTokens = calculateMaxTokensBasedOnImageSize(melSpectrogramPath);

        // 프롬프트 생성
        String prompt = String.format("다음 문장에 대한 피드백을 제공하세요: %s\n주파수 대역 특징: %s\n위의 문장 피드백과 관련지어 FFT 스펙트로그램을 Base64 인코딩 한 정보의 주파수 대역 변화만으로 감정을 나눠서 감정 변화에 대한 피드백을 제공하세요.: %s",
                sttResult, features, melSpectrogramBase64.substring(0, 1000));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", new Object[]{new HashMap<String, String>() {{
            put("role", "user");
            put("content", prompt);
        }}});
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            logger.info("GPT API 요청 본문: {}", requestBody);
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, requestEntity, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    return content.trim();  // 피드백 내용 반환
                } else {
                    logger.error("응답의 선택 항목이 비어 있습니다.");
                    throw new RuntimeException("응답의 선택 항목이 비어 있습니다.");
                }
            } else {
                logger.error("응답 본문이 비어 있습니다.");
                throw new RuntimeException("응답 본문이 비어 있습니다.");
            }
        } catch (RestClientException e) {
            logger.error("GPT API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("GPT API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 이미지 크기에 따라 max_tokens 계산
    private int calculateMaxTokensBasedOnImageSize(String melSpectrogramPath) {
        File file = new File(melSpectrogramPath);
        long fileSizeInBytes = file.length();
        if (fileSizeInBytes < 100 * 1024) { // 100KB
            return 300;
        } else {
            return 500;
        }
    }
}


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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GPTService {

    private static final Logger logger = LoggerFactory.getLogger(GPTService.class);

    // Dotenv을 사용하여 .env 파일에서 API 키 로드
    private final String apiKey;

    // 기본 URL.
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

    public String getFeedback(String sttResult) {

        // 프롬프트 생성
        String prompt = String.format("다음 문장에 대한 <피드백>을 제공하세요. 이때 감정을 잘 표출하는 방법에 대한 내용도 넣어 주세요. 만약 문장에 잘못된 점이 없다면 '그렇게 말하면 될 것같아요' 라고 해주세요.: %s\n\n <피드백> 후 %s와 연결된 상대방의 <다음 대화>를 주세요. <피드백> 후 <다음 대화>는 줄바꿈되어 보이도록 해주세요.",
                sttResult,sttResult);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", new Object[]{new HashMap<String, String>() {{
            put("role", "user");
            put("content", prompt);
        }}});
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
}



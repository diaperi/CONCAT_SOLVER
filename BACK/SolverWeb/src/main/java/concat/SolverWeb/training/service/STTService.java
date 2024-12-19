package concat.SolverWeb.training.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class STTService {

    private static final Logger logger = LoggerFactory.getLogger(STTService.class);

    private final WebClient webClient;
    private final String invokeUrl;
    private final String secret;

    // .env 파일에서 환경 변수 읽기
    public STTService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        Dotenv dotenv = Dotenv.load(); // spring-dotenv로 .env 파일 로드
        this.invokeUrl = dotenv.get("CLOVA_SPEECH_INVOKE_URL");
        this.secret = dotenv.get("CLOVA_SPEECH_SECRET");
    }

    public String uploadAudioAndGetText(MultipartFile audioFile) throws IOException {
        byte[] audioBytes = audioFile.getBytes();
        MultiValueMap<String, Object> body = getMultipartBody(audioBytes);

        try {
            String response = this.webClient.post()
                    .uri(invokeUrl + "/recognizer/upload")
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header("X-CLOVASPEECH-API-KEY", secret)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response);
            return jsonResponse.path("text").asText();
        } catch (Exception e) {
            logger.error("STT API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("STT API 호출 중 오류가 발생했습니다.", e);
        }
    }

    private MultiValueMap<String, Object> getMultipartBody(byte[] audioBytes) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Add audio file
        body.add("media", new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return "audio.wav";
            }
        });

        // Add additional parameters as JSON
        Map<String, Object> params = new HashMap<>();
        params.put("language", "ko-KR");
        params.put("completion", "sync");
        params.put("wordAlignment", true);
        params.put("fullText", true);
        params.put("noiseFiltering", true);

        body.add("params", params); // params는 직접 추가

        return body;
    }
}

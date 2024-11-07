// 주석 처리 - 주파수 스펙트로그램 관련 코드
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
                return "audio.wav"; // 파일 이름 지정
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

// *******************************************************************************//
//    // 기존 파일 삭제 메서드
//    private void deleteOldSpectrogramImage(String filePath) {
//        File file = new File(filePath);
//        if (file.exists()) {
//            if (file.delete()) {
//                logger.info("이전 FFT 스펙트로그램 이미지가 삭제되었습니다: {}", filePath);
//            } else {
//                logger.warn("이전 FFT 스펙트로그램 이미지를 삭제할 수 없습니다: {}", filePath);
//            }
//        }
//    }
//
//    public String generateMelSpectrogram(byte[] audioData, String s) throws IOException {
//        logger.info("FFT를 통한 주파수 스펙트럼 생성 중...");
//
//        // FFT 분석 이미지 저장 경로
//        String fftSpectrogramPath = "D:\\CONCAT\\CONCAT_SOLVER\\BACK\\SolverWeb\\src\\main\\resources\\static\\media\\fft_spectrogram.png";
//
//        // 기존 스펙트로그램 파일 삭제
//        deleteOldSpectrogramImage(fftSpectrogramPath);
//
//        // FFT 및 주파수 분석에 필요한 설정
//        int fftSize = 2048; // FFT 크기
//        int hopSize = 512; // hop size
//        int sampleRate = 48000; // 샘플링 주파수
//
//        // FFT 결과 배열
//        double[][] fftSpectrogram = new double[(audioData.length / hopSize) + 1][fftSize / 2];
//
//        double maxFFTValue = Double.MIN_VALUE; // 최대 FFT 값을 찾기 위한 변수
//
//        // 각 프레임의 FFT 계산
//        for (int i = 0; i < audioData.length - fftSize; i += hopSize) {
//            double[] frame = new double[fftSize];
//            for (int j = 0; j < fftSize; j++) {
//                frame[j] = audioData[i + j]; // 오디오 데이터의 각 프레임을 가져옴
//            }
//
//            // FFT 수행
//            Complex[] fftResult = performFFT(frame);
//
//            // FFT 결과 저장 및 로그 출력
//            for (int k = 0; k < fftSize / 2; k++) {
//                double magnitude = fftResult[k].abs();
//                fftSpectrogram[i / hopSize][k] = Math.log1p(magnitude); // 로그 스케일 적용
//                if (magnitude > maxFFTValue) {
//                    maxFFTValue = magnitude; // 최대 FFT 값을 업데이트
//                }
//            }
//        }
//
//        // 최대 FFT 값 로그로 출력
//        logger.info("FFT 스펙트럼에서 최대값: {}", maxFFTValue);
//
//        // FFT 스펙트로그램을 이미지로 저장
//        saveFFTSpectrogramImage(fftSpectrogram, fftSpectrogramPath, maxFFTValue, hopSize, sampleRate); // 최대값을 이미지 저장 시 사용
//
//        return fftSpectrogramPath;
//    }
//
//
//    private Complex[] performFFT(double[] frame) {
//        // FFT 수행
//        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
//        return transformer.transform(frame, TransformType.FORWARD);
//    }
//
//    // 지피티 토큰 수를 줄이기 위한 이미지 크기 줄이기
//    private void saveFFTSpectrogramImage(double[][] fftSpectrogram, String fftSpectrogramPath, double maxFFTValue, double hopSize, double sampleRate) throws IOException {
//        int width = fftSpectrogram.length; // 시간축 길이 (프레임 수)
//        int height = fftSpectrogram[0].length; // 주파수 축 길이 (FFT 크기의 절반)
//
//        // 여백과 색상 축 공간을 줄임
//        BufferedImage image = new BufferedImage(width + 100, height + 50, BufferedImage.TYPE_INT_RGB); // 여백을 줄여 이미지 크기 축소
//        Graphics2D g = image.createGraphics();
//
//        // 배경색 설정 (흰색)
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, width + 100, height + 50);
//
//        // 폰트 크기를 더 작게 조정
//        g.setFont(new Font("Arial", Font.PLAIN, 12)); // 폰트 크기를 12로 설정
//
//        // FFT 스펙트로그램 그리기 (HSB 색상 팔레트 적용)
//        for (int t = 0; t < fftSpectrogram.length; t++) {
//            for (int f = 0; f < fftSpectrogram[t].length; f++) {
//                double normalizedValue = fftSpectrogram[t][f] / Math.log1p(maxFFTValue);
//                normalizedValue = Math.min(Math.max(normalizedValue, 0), 1); // 0~1 사이로 클리핑
//
//                // HSB 색상 팔레트를 사용하여 컬러 설정
//                float hue = (float) (normalizedValue * 0.7); // 0 ~ 0.7 범위에서 색상 생성
//                Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
//                g.setColor(color);
//
//                // 스펙트로그램에 픽셀 그리기
//                g.fillRect(t + 50, height - f, 1, 1); // 좌표계 맞추기 (x: 시간, y: 주파수)
//            }
//        }
//
//        // 시간 축에 그리드 추가 (그리드 라인은 연한 회색)
//        g.setColor(Color.LIGHT_GRAY);
//        for (int t = 0; t <= width; t += width / 10) {
//            g.drawLine(t + 50, 0, t + 50, height); // 세로 그리드 라인 그리기
//        }
//
//        // 주파수 축에 그리드 추가
//        for (int f = 0; f <= height; f += height / 10) {
//            g.drawLine(50, height - f, width + 50, height - f); // 가로 그리드 라인 그리기
//        }
//
//        // 시간 축 그리기
//        g.setColor(Color.BLACK);
//        g.drawString("Time (s)", width / 2, height + 25); // 시간축 레이블
//        for (int t = 0; t <= width; t += width / 10) {
//            double timeInSeconds = (double) t * hopSize / sampleRate;
//            g.drawString(String.format("%.2f", timeInSeconds), t + 50, height + 20); // 시간값 표시
//            g.drawLine(t + 50, height, t + 50, height + 5); // 시간축 틱 표시
//        }
//
//        // 주파수 축 그리기
//        g.drawString("Frequency (Hz)", 15, height / 2); // 주파수축 레이블
//        for (int f = 0; f <= height; f += height / 10) {
//            double frequencyInHz = (double) f * sampleRate / (2 * height);
//            g.drawString(String.format("%.0f", frequencyInHz), 5, height - f + 5); // 주파수값 표시
//            g.drawLine(45, height - f, 50, height - f); // 주파수축 틱 표시
//        }
//
//        // 색상바 추가 (오른쪽에 작은 색상바)
//        int colorBarX = width + 60; // 색상바 위치
//        int colorBarHeight = height;
//        for (int i = 0; i < colorBarHeight; i++) {
//            float hue = (float) i / colorBarHeight * 0.7f;
//            Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
//            g.setColor(color);
//            g.fillRect(colorBarX, height - i, 10, 1); // 색상바 그리기
//        }
//
//        // 색상바에 주파수 표시
//        g.setColor(Color.BLACK);
//        g.drawString("Frequency (Hz)", colorBarX - 5, height + 20); // 색상바 주파수 축 레이블
//        for (int i = 0; i <= 10; i++) {
//            int y = height - (i * height / 10);
//            double freq = (double) i * sampleRate / 2 / 10;
//            g.drawString(String.format("%.0f", freq), colorBarX + 15, y + 5); // 주파수값 표시
//        }
//
//        g.dispose();
//
//        // 이미지 저장
//        ImageIO.write(image, "png", new File(fftSpectrogramPath));
//        logger.info("FFT 스펙트로그램 이미지가 저장되었습니다: {}", fftSpectrogramPath);
//    }
//}

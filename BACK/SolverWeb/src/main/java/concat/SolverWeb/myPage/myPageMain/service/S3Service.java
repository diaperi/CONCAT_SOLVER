package concat.SolverWeb.myPage.myPageMain.service;

import concat.SolverWeb.myPage.myPageMain.controller.MyPageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import javax.annotation.PostConstruct;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    private final Set<String> processedFiles = new HashSet<>();

    private S3Client s3Client;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @PostConstruct
    public void initialize() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create("https://s3.ap-northeast-2.amazonaws.com"))
                .build();
    }

    // 파일 이름에 따라 타임스탬프 추출 방식을 결정하는 메서드
    private String extractTimestamp(String fileName) {
        if (fileName.contains("first_frame_")) {
            return extractTimestampFromImage(fileName);
        } else if (fileName.contains("negative_emotion_")) {
            return extractTimestampFromVideo(fileName);
        } else if (fileName.contains("gpt_response_")) {
            return extractTimestampFromGpt(fileName);
        } else {
            logger.error("타임스탬프 추출 실패: 파일 형식이 잘못되었습니다: " + fileName);
            return "invalid-timestamp";
        }
    }

    // GPT 응답 파일 (gpt_response_YYYYMMDD_HHMMSS.txt)에서 타임스탬프 추출
    private String extractTimestampFromGpt(String gptKey) {
        try {
            int startIndex = gptKey.indexOf("gpt_response_") + 13;
            int endIndex = gptKey.indexOf(".txt");
            if (startIndex < 13 || endIndex == -1 || startIndex >= endIndex) {
                logger.error("타임스탬프 추출 실패: gptKey 형식이 잘못되었습니다: " + gptKey);
                return "invalid-timestamp";
            }
            return gptKey.substring(startIndex, endIndex);
        } catch (Exception e) {
            logger.error("타임스탬프 추출 실패: " + e.getMessage());
            return "invalid-timestamp";
        }
    }

    // 이미지 파일 (first_frame_YYYYMMDD_HHMMSS.jpg)에서 타임스탬프 추출
    private String extractTimestampFromImage(String imageKey) {
        try {
            int startIndex = imageKey.indexOf("first_frame_") + 12;
            int endIndex = imageKey.indexOf(".jpg");
            if (startIndex < 12 || endIndex == -1 || startIndex >= endIndex) {
                logger.error("타임스탬프 추출 실패: imageKey 형식이 잘못되었습니다: " + imageKey);
                return "invalid-timestamp";
            }
            return imageKey.substring(startIndex, endIndex); // "YYYYMMDD_HHMMSS" 형식으로 반환
        } catch (Exception e) {
            logger.error("타임스탬프 추출 실패: " + e.getMessage());
            return "invalid-timestamp";
        }
    }


    // 동영상 파일 (negative_emotion_YYYYMMDD_HHMMSS_converted.mp4)에서 타임스탬프 추출
    public String extractTimestampFromVideo(String videoKey) {
        try {
            int startIndex = videoKey.indexOf("negative_emotion_") + 17;
            int endIndex = videoKey.indexOf("_converted.mp4");
            if (startIndex < 17 || endIndex == -1 || startIndex >= endIndex) {
                logger.error("타임스탬프 추출 실패: videoKey 형식이 잘못되었습니다: " + videoKey);
                return "invalid-timestamp";
            }
            return videoKey.substring(startIndex, endIndex);
        } catch (Exception e) {
            logger.error("타임스탬프 추출 실패: " + e.getMessage());
            return "invalid-timestamp";
        }
    }

    // 특정 사용자의 폴더 내 이미지 목록 출력
    public List<ImageInfo> getAllImagesSortedByLatest(String userId) {
        String userFolderPrefix = userId + "/videos/";

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userFolderPrefix)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH:mm:ss"); // 날짜와 시간 포함

        return response.contents().stream()
                .filter(s -> s.key().endsWith(".jpg") || s.key().endsWith(".jpeg") || s.key().endsWith(".png"))
                .sorted(Comparator.comparing(S3Object::lastModified).reversed())
                .map(s -> {
                    String imageUrl = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s.key())).toExternalForm();
                    String lastModifiedDate = ZonedDateTime.ofInstant(s.lastModified(), ZoneId.systemDefault()).format(formatter);

                    // GPT 제목 가져오기
                    String gptTitle = getGptTitleFromImageTimestamp(userId, s.key());

                    return new ImageInfo(imageUrl, lastModifiedDate, s.size(), s.key(), gptTitle);
                })
                .collect(Collectors.toList());
    }

    // GPT 제목을 추출하는 메서드
    private String getGptTitleFromImageTimestamp(String userId, String imageKey) {
        try {
            String timestamp = extractTimestamp(imageKey);
            if ("invalid-timestamp".equals(timestamp)) {
                return "제목 없음";
            }

            String userFolderPrefix = userId + "/gpt/";

            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(userFolderPrefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

            Optional<S3Object> gptFile = response.contents().stream()
                    .filter(s -> s.key().contains(timestamp) && s.key().endsWith(".txt"))
                    .findFirst();

            if (gptFile.isPresent()) {
                String key = gptFile.get().key();
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                ResponseInputStream<GetObjectResponse> s3ObjectInputStream = s3Client.getObject(getObjectRequest);
                String content = new BufferedReader(new InputStreamReader(s3ObjectInputStream))
                        .lines()
                        .collect(Collectors.joining("\n"));

                Pattern pattern = Pattern.compile("<(.*?)>");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    return matcher.group(1); // 제목 반환
                }
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve GPT title from S3.", e);
        }

        return "제목 없음"; // 예외 발생 시 기본 제목 반환
    }


    // 특정 사용자의 폴더 내에서 날짜에 해당하는 영상 가져오기
    public Optional<ImageInfo> getVideoByDate(String userId, String date) {
        String userFolderPrefix = userId + "/videos/";

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userFolderPrefix)  // 특정 사용자의 폴더에서만 파일 검색
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

        return response.contents().stream()
                .filter(s -> s.key().endsWith(".mp4"))
                .filter(s -> {
                    // 파일명에서 날짜 추출
                    String fileDate = s.key().substring(s.key().indexOf("negative_emotion_") + 17, s.key().indexOf("negative_emotion_") + 25);
                    return fileDate.equals(date);  // 요청된 날짜와 파일의 날짜 비교
                })
                .max(Comparator.comparing(S3Object::lastModified))  // 가장 최근의 파일 선택
                .map(s -> {
                    // GPT 텍스트 파일의 제목을 추출합니다.
                    String gptTitle = getGptTitleFromImageTimestamp(userId, s.key());

                    return new ImageInfo(
                            s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s.key())).toExternalForm(),
                            ZonedDateTime.ofInstant(s.lastModified(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")),
                            s.size(),
                            s.key(),
                            gptTitle // 제목을 추가합니다.
                    );
                });
    }

    // 특정 사용자의 폴더 내에서 타임스탬프에 해당하는 영상 가져오기
    public Optional<ImageInfo> getVideoByTimestamp(String userId, String timestamp) {
        String userFolderPrefix = userId + "/videos/";

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userFolderPrefix)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

        return response.contents().stream()
                .filter(s -> s.key().contains(timestamp) && s.key().endsWith("_converted.mp4"))
                .map(s -> {
                    // GPT 제목을 추출합니다.
                    String gptTitle = getGptTitleFromImageTimestamp(userId, s.key());

                    return new ImageInfo(
                            s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s.key())).toExternalForm(),
                            ZonedDateTime.ofInstant(s.lastModified(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")),
                            s.size(),
                            s.key(),
                            gptTitle // GPT 제목을 추가합니다.
                    );
                })
                .findFirst();
    }

    // 특정 영상 파일의 타임스탬프에 해당하는 텍스트 파일 가져오기
    public Optional<String> getTranscriptByVideoTimestamp(String userId, String timestamp) {
        try {
            String userFolderPrefix = userId + "/done/";
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(userFolderPrefix)
                    .build();
            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);
            Optional<S3Object> matchingTranscript = response.contents().stream()
                    .filter(s -> s.key().contains(timestamp) && s.key().endsWith("_transcript.txt"))
                    .findFirst();

            if (matchingTranscript.isPresent()) {
                String key = matchingTranscript.get().key();
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                ResponseInputStream<GetObjectResponse> s3ObjectInputStream = s3Client.getObject(getObjectRequest);
                String content = new BufferedReader(new InputStreamReader(s3ObjectInputStream))
                        .lines()
                        .collect(Collectors.joining("\n"));

                return Optional.of(content);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve the transcript content from S3.", e);
            return Optional.empty();
        }
    }

    // 특정 영상 파일의 타임스탬프에 해당하는 GPT 응답 파일 가져오기
    public Optional<String> getGptResponseByVideoTimestamp(String userId, String timestamp) {
        try {
            String userFolderPrefix = userId + "/gpt/";
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(userFolderPrefix)
                    .build();
            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);
            Optional<S3Object> matchingGptResponse = response.contents().stream()
                    .filter(s -> s.key().contains(timestamp) && s.key().endsWith(".txt"))
                    .findFirst();

            if (matchingGptResponse.isPresent()) {
                String key = matchingGptResponse.get().key();
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                ResponseInputStream<GetObjectResponse> s3ObjectInputStream = s3Client.getObject(getObjectRequest);
                String content = new BufferedReader(new InputStreamReader(s3ObjectInputStream))
                        .lines()
                        .collect(Collectors.joining("\n"));

                // 제목 끝의 '>' 뒤에 <br> 태그 추가
                String formattedContent = content.replaceAll(">([^\\s])", "><br><br>$1");

                // .을 기준으로 <br> 태그 추가
                formattedContent = formattedContent.replaceAll("\\.", ".<br><br>");

                // formattedContent를 반환합니다.
                return Optional.of(formattedContent);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve the GPT response content from S3.", e);
            return Optional.empty();
        }
    }

    // 최신 영상 파일을 가져오기 위한 메서드
    public Optional<ImageInfo> getLatestVideo(String userId) {
        String userFolderPrefix = userId + "/videos/";

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userFolderPrefix)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

        return response.contents().stream()
                .filter(s -> s.key().endsWith("_converted.mp4"))
                .max(Comparator.comparing(S3Object::lastModified))
                .map(s -> {
                    // GPT 제목을 추출합니다.
                    String gptTitle = getGptTitleFromImageTimestamp(userId, s.key());

                    return new ImageInfo(
                            s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s.key())).toExternalForm(),
                            ZonedDateTime.ofInstant(s.lastModified(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")),
                            s.size(),
                            s.key(),
                            gptTitle // GPT 제목을 추가합니다.
                    );
                });
    }

    // S3 폴더를 10초마다 감시하는 스케줄러
    @Scheduled(fixedDelay = 10000)  // 10초마다 실행
    public void checkForNewFiles() {
        String userId = "yourUserId"; // 사용자의 ID를 여기서 가져오거나 세션에서 처리

        // S3에서 최신 파일을 가져옴
        Optional<ImageInfo> latestVideo = getLatestVideo(userId);

        if (latestVideo.isPresent()) {
            String videoTimestamp = extractTimestamp(latestVideo.get().getKey());

            // 기존에 처리한 파일인지 확인
            if (!processedFiles.contains(videoTimestamp)) {
                Optional<String> transcriptContent = getTranscriptByVideoTimestamp(userId, videoTimestamp);
                Optional<String> gptContent = getGptResponseByVideoTimestamp(userId, videoTimestamp);

                if (transcriptContent.isPresent() && !processedFiles.contains(transcriptContent.get())) {
                    logger.info("New transcript content detected.");
                    processedFiles.add(transcriptContent.get());
                    // 여기서 텍스트 파일을 처리합니다 (예: 클라이언트로 전송 등)
                }

                if (gptContent.isPresent() && !processedFiles.contains(gptContent.get())) {
                    logger.info("New GPT response content detected.");
                    processedFiles.add(gptContent.get());
                    // 여기서 GPT 결과를 처리합니다 (예: 클라이언트로 전송 등)
                }
            }
        }
    }

    public static class ImageInfo {
        private String url;
        private String lastModifiedDate;
        private long size;
        private String key;
        private String gptTitle; // 제목 필드 추가

        // 기존 생성자에 gptTitle 초기화 추가
        public ImageInfo(String url, String lastModifiedDate, long size, String key, String gptTitle) {
            this.url = url;
            this.lastModifiedDate = lastModifiedDate;
            this.size = size;
            this.key = key;
            this.gptTitle = gptTitle;
        }

        public String getUrl() {
            return url;
        }

        public String getLastModifiedDate() {
            return lastModifiedDate;
        }

        public long getSize() {
            return size;
        }

        public String getKey() {
            return key;
        }

        public String getGptTitle() {
            return gptTitle;
        }

        public void setGptTitle(String gptTitle) {
            this.gptTitle = gptTitle;
        }

        public String getFormattedSize() {
            if (size < 1024) return size + "B";
            int exp = (int) (Math.log(size) / Math.log(1024));
            return String.format("%.1f%sB", size / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
        }
    }


    public List<ImageInfo> searchImagesByKeyword(String userId, String keyword) {
        String userFolderPrefix = userId + "/videos/";

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userFolderPrefix)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

        return response.contents().stream()
                .filter(s -> s.key().endsWith(".jpg") || s.key().endsWith(".jpeg") || s.key().endsWith(".png"))
                .map(s -> {
                    String imageUrl = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s.key())).toExternalForm();
                    String lastModifiedDate = ZonedDateTime.ofInstant(s.lastModified(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy.MM.dd_HH:mm:ss"));
                    String gptTitle = getGptTitleFromImageTimestamp(userId, s.key());
                    return new ImageInfo(imageUrl, lastModifiedDate, s.size(), s.key(), gptTitle);
                })
                .filter(imageInfo ->
                        imageInfo.getGptTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                imageInfo.getLastModifiedDate().contains(keyword))
                .collect(Collectors.toList());
    }

    // 이미지 감정 분석 수행 메서드
    public String generateImageEmotionAnalysis(String s3Key, String participant, String outputPath) {
        try {
            // Python 명령어와 인수를 설정
            List<String> commands = new ArrayList<>();
            commands.add("D:\\pythonProject\\testProject\\venv\\Scripts\\python.exe"); // 가상환경의 Python 경로
            commands.add("src/main/resources/scripts/emotionChart.py"); // Python 스크립트의 경로
            commands.add("diaperiwinklebucket2"); // S3 버킷 이름
            commands.add(s3Key); // S3 경로
            commands.add(participant); // 분석할 참여자
            commands.add(outputPath); // 결과 이미지 경로

            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line); // 스크립트 출력을 로깅합니다.
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Python script executed successfully.");
                return outputPath;
            } else {
                logger.error("Python script execution failed with exit code: " + exitCode);
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to execute Python script for emotion analysis.", e);
            return null;
        }
    }

    // 텍스트 감정 분석 수행 메서드
    public String generateTextEmotionAnalysis(String s3Key) {
        try {
            // Python 명령어와 인수를 설정
            List<String> commands = new ArrayList<>();
            commands.add("D:\\pythonProject\\testProject\\venv\\Scripts\\python.exe"); // 가상환경의 Python 경로
            commands.add("src/main/resources/scripts/emotionAdjust.py"); // Python 스크립트의 경로
            commands.add("diaperiwinklebucket2"); // S3 버킷 이름
            commands.add(s3Key); // S3 경로

            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 여기서 UTF-8 인코딩을 지정하여 출력을 읽음
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Python script executed successfully.");
                String result = output.toString().trim();

                // '.' 기준으로 줄 바꿈 처리
                result = result.replace(".", ".<br>");

                logger.info("Script output: " + result);
                return result; // 텍스트 결과 반환
            } else {
                logger.error("Python script execution failed with exit code: " + exitCode);
                logger.error("Script error output: " + errorOutput.toString());
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to execute Python script for text emotion analysis.", e);
            return null;
        }
    }


    public Optional<String> getLatestTranscript(String userId) {
        try {
            // 사용자의 폴더 경로 지정
            String userFolderPrefix = userId + "/done/";

            // S3에서 객체 목록을 가져옵니다.
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(userFolderPrefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

            // 최신 텍스트 파일을 찾습니다.
            Optional<S3Object> latestTranscript = response.contents().stream()
                    .filter(s -> s.key().endsWith("_transcript.txt"))  // 텍스트 파일 필터링
                    .max(Comparator.comparing(S3Object::lastModified));  // 가장 최근 파일 찾기

            if (latestTranscript.isPresent()) {
                String key = latestTranscript.get().key(); // S3 경로를 반환
                return Optional.of(key);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve the latest transcript from S3.", e);
            return Optional.empty();
        }
    }


    //    제목추출
    public List<Map<String, String>> getGptTitlesByDateRange(String userId, String startDate, String endDate, int page, int size) {
        try {
            String userFolderPrefix = userId + "/gpt/";

            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(userFolderPrefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

            List<S3Object> filteredContents = response.contents().stream()
                    .filter(s -> s.key().endsWith(".txt"))
                    .filter(s -> {
                        Map<String, String> dateTimeMap = extractDateTimeFromGptKey(s.key());
                        String fileDate = dateTimeMap.get("date");
                        return fileDate != null && fileDate.compareTo(startDate) >= 0 && fileDate.compareTo(endDate) <= 0;
                    })
                    .sorted(Comparator.comparing(S3Object::lastModified).reversed())
                    .skip((long) (page - 1) * size) // 페이지 번호에 따른 건너뛰기
                    .limit(size) // 한 페이지에 보여줄 개수 제한
                    .collect(Collectors.toList());

            if (filteredContents.isEmpty()) {
                logger.warn("No data found for the given date range: {} to {}", startDate, endDate);
            }

            return filteredContents.stream()
                    .map(s -> {
                        String title = getGptTitleFromFile(s.key());
                        Map<String, String> dateTimeMap = extractDateTimeFromGptKey(s.key());
                        Map<String, String> resultMap = new HashMap<>();
                        resultMap.put("title", title);
                        resultMap.put("date", dateTimeMap.get("date"));
                        resultMap.put("time", dateTimeMap.get("time"));
                        return resultMap;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to retrieve GPT titles by date range.", e);
            return Collections.emptyList();
        }
    }

    private Map<String, String> extractDateTimeFromGptKey(String gptKey) {
        Map<String, String> dateTimeMap = new HashMap<>();
        try {
            Pattern pattern = Pattern.compile("gpt_response_(\\d{8})_(\\d{6})\\.txt");
            Matcher matcher = pattern.matcher(gptKey);
            if (matcher.find()) {
                String date = matcher.group(1); // YYYYMMDD 형식
                String time = matcher.group(2); // HHMMSS 형식
                dateTimeMap.put("date", date);
                dateTimeMap.put("time", time);
            }
        } catch (Exception e) {
            logger.error("Failed to extract date and time from GPT key: {}", gptKey, e);
        }
        return dateTimeMap;
    }

    private String getGptTitleFromFile(String s3Key) {
        try (ResponseInputStream<GetObjectResponse> s3ObjectInputStream = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucketName).key(s3Key).build())) {

            String content = new BufferedReader(new InputStreamReader(s3ObjectInputStream))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // <제목> 태그에서 제목 추출
            Pattern pattern = Pattern.compile("<(.*?)>");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1); // 제목 반환
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve GPT title from S3 for key: {}", s3Key, e);
        }
        return "제목 없음";
    }


    //    ***********대화재구성*********
    //    ***********대화재구성*********
    public Map<String, String> processDialogue(String userId, String date) {
        try {
            // S3 경로 구성
            String s3PathPrefix = String.format("%s/done/negative_emotion_%s", userId, date);

            // S3에서 해당 경로에 있는 파일 목록 가져오기
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(s3PathPrefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);
            List<S3Object> files = response.contents();

            logger.info("S3 Path Prefix: " + s3PathPrefix);
            logger.info("Number of files found: " + files.size());
            files.forEach(file -> logger.info("Found file: " + file.key()));

            if (files.isEmpty()) {
                logger.warn("No files found for the specified date: " + date);
                return Map.of("original", "No matching files found.", "result", "");
            }

            // 파일 목록 중 가장 최신의 파일을 선택
            S3Object latestFile = Collections.max(files, Comparator.comparing(S3Object::lastModified));
            String latestFileKey = latestFile.key();

            logger.info("Latest file selected: " + latestFileKey);

            // S3에서 원본 텍스트 가져오기
            String originalContent = getFileFromS3(bucketName, latestFileKey);

// Python 스크립트 실행 명령어 구성
            List<String> commands = new ArrayList<>();
            commands.add("D:\\pythonProject\\testProject\\venv\\Scripts\\python.exe"); // 가상환경의 Python 경로
            commands.add("src/main/resources/scripts/recommendText.py"); // Python 스크립트의 경로

            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);

            Process process = pb.start();

// 원본 텍스트를 UTF-8로 인코딩하여 파이썬 프로세스의 입력 스트림에 전달
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(originalContent);
                writer.flush();
            }


            // Python 스크립트의 출력을 UTF-8로 읽어들임
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder processedContent = new StringBuilder();
            String result;
            while ((result = reader.readLine()) != null) {
                processedContent.append(result).append("\n");
            }

            String finalResult = processedContent.toString().trim();

            // 결과를 반환할 Map 구성 (로그에 출력하지 않음)
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("original", originalContent);
            responseMap.put("result", finalResult);

            return responseMap;

        } catch (Exception e) {
            logger.error("Error during processing dialogue", e);
            return Map.of("original", "Error occurred", "result", "");
        }
    }


    // S3에서 파일 가져오기 함수
    private String getFileFromS3(String bucketName, String fileKey) {
        try {
            // S3에서 객체 가져오기
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            // S3 객체를 ResponseInputStream으로 가져오기
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

            // InputStream을 읽어들여서 문자열로 변환
            String content = new BufferedReader(new InputStreamReader(s3Object, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            return content;
        } catch (Exception e) {
            logger.error("Error fetching file from S3: " + e.getMessage(), e);
            return "Error occurred while fetching the original file.";
        }
    }
}
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

import io.github.cdimascio.dotenv.Dotenv;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    private final Set<String> processedFiles = new HashSet<>();

    private S3Client s3Client;

    private String accessKey;
    private String secretKey;
    private String region;
    private String bucketName;

    private String openaiApiKey;

    // 생성자에서 .env 파일 로드
    public S3Service() {
        try {
            // .env 파일에서 환경 변수를 로드합니다.
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            // .env 파일에서 값이 없으면 System.getenv()로 대체
            this.accessKey = dotenv.get("AWS_ACCESS_KEY_ID", System.getenv("AWS_ACCESS_KEY_ID"));
            this.secretKey = dotenv.get("AWS_SECRET_ACCESS_KEY", System.getenv("AWS_SECRET_ACCESS_KEY"));
            this.region = dotenv.get("AWS_REGION", System.getenv("AWS_REGION"));
            this.bucketName = dotenv.get("AWS_BUCKET_NAME", System.getenv("AWS_BUCKET_NAME"));

            // 로그로 환경 변수 값 출력
            logger.info("AccessKey: {}", accessKey);
            logger.info("SecretKey: {}", secretKey);
            logger.info("Region: {}", region);
            logger.info("BucketName: {}", bucketName);
        } catch (Exception e) {
            logger.error("Error loading environment variables from .env file", e);
        }
    }

    // PostConstruct로 S3Client 초기화
    @PostConstruct
    public void initialize() {
        try {
            // AWS 자격 증명 설정
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
            this.s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                    .build();

            // OpenAI API 키 설정 및 로그 추가
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            this.openaiApiKey = dotenv.get("OPENAI_API_KEY", System.getenv("OPENAI_API_KEY"));

            // 추가된 로그: Dotenv와 System.getenv()에서 가져온 값 출력
            logger.info("OPENAI_API_KEY from Dotenv: {}", dotenv.get("OPENAI_API_KEY")); // Dotenv에서 가져온 값
            logger.info("OPENAI_API_KEY from System.getenv: {}", System.getenv("OPENAI_API_KEY")); // 시스템 환경 변수에서 가져온 값
            logger.info("Current directory: " + System.getProperty("user.dir"));

            // OpenAI API 키 검증
            if (openaiApiKey == null || openaiApiKey.isEmpty()) {
                logger.error("OPENAI_API_KEY is not set or is empty!");
            } else {
                logger.info("OPENAI_API_KEY: {}", openaiApiKey);
            }

            logger.info("S3Client initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing S3Client", e);
        }
    }


//    @Value("${cloud.aws.credentials.accessKey}")
//    private String accessKey;
//
//    @Value("${cloud.aws.credentials.secretKey}")
//    private String secretKey;
//
//    @Value("${cloud.aws.region.static}")
//    private String region;
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucketName;
//
//    @PostConstruct
//    public void initialize() {
//        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
//        this.s3Client = S3Client.builder()
//                .region(Region.of(region))
//                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
//                .endpointOverride(URI.create("https://s3.ap-northeast-2.amazonaws.com"))
//                .build();
//    }

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
    public Map<String, Object> getGptResponseByVideoTimestamp(String userId, String timestamp) {
        Map<String, Object> responseMap = new HashMap<>();
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

                // . 뒤에 <br><br> 추가
                content = content.replaceAll("\\.(?!\\s*$)", ".<br><br>");

                // 제목, 요약, 참여자 솔루션 파싱
                String[] sections = content.split("\n\n");

                // 첫 번째 줄은 제목
                responseMap.put("gptTitle", sections.length > 0 ? sections[0].trim() : "제목 없음");

                // 두 번째 섹션은 요약
                responseMap.put("gptSummary", sections.length > 1 ? sections[1].trim() : "요약 없음");

                // 참여자별 솔루션 파싱
                Map<String, String> participantSolutions = new LinkedHashMap<>();
                for (int i = 2; i < sections.length; i++) {
                    String section = sections[i].trim();
                    if (section.startsWith("참여자")) {
                        String participantKey = "🧑 참여자" + (participantSolutions.size() + 1);
                        participantSolutions.put(participantKey, section);
                    }
                }
                responseMap.put("participants", participantSolutions);

                return responseMap;
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve the GPT response content from S3.", e);
        }
        return responseMap;
    }

    // 특정 영상 파일의 타임스탬프에 해당하는 동영상 파일 가져오기(AIVIDEO)
    public Optional<ImageInfo> getAIVideoByTimestamp(String userId, String timestamp) {
        try {
            // S3 버킷 내 사용자 폴더 경로 설정
            String userFolderPrefix = userId + "/aiVideo/";

            // S3에서 파일 목록 가져오기
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(userFolderPrefix)
                    .build();
            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

            // 타임스탬프와 확장자(.mp4)에 해당하는 파일 필터링
            Optional<S3Object> matchingVideo = response.contents().stream()
                    .filter(s -> s.key().contains(timestamp) && s.key().endsWith(".mp4"))
                    .findFirst();

            if (matchingVideo.isPresent()) {
                // 매칭된 파일이 존재하면 S3 객체 키 가져오기
                S3Object videoObject = matchingVideo.get();
                String key = videoObject.key();
                long size = videoObject.size(); // 파일 크기
                String lastModifiedDate = videoObject.lastModified().toString(); // 마지막 수정 시간

                // 동영상 URL 생성
                String videoUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()).toExternalForm();

                // 결과 객체 생성 및 반환
                return Optional.of(new ImageInfo(videoUrl, lastModifiedDate, size, key, "AI 동영상")); // gptTitle은 "AI 동영상"으로 설정
            } else {
                return Optional.empty(); // 매칭되는 동영상이 없을 경우
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve the video content from S3.", e);
            return Optional.empty();
        }
    }


//    public Optional<String> getGptResponseByVideoTimestamp(String userId, String timestamp) {
//        try {
//            String userFolderPrefix = userId + "/gpt/";
//            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
//                    .bucket(bucketName)
//                    .prefix(userFolderPrefix)
//                    .build();
//            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);
//            Optional<S3Object> matchingGptResponse = response.contents().stream()
//                    .filter(s -> s.key().contains(timestamp) && s.key().endsWith(".txt"))
//                    .findFirst();
//
//            if (matchingGptResponse.isPresent()) {
//                String key = matchingGptResponse.get().key();
//                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//                        .bucket(bucketName)
//                        .key(key)
//                        .build();
//
//                ResponseInputStream<GetObjectResponse> s3ObjectInputStream = s3Client.getObject(getObjectRequest);
//                String content = new BufferedReader(new InputStreamReader(s3ObjectInputStream))
//                        .lines()
//                        .collect(Collectors.joining("\n"));
//
//                // 제목 끝의 '>' 뒤에 <br> 태그 추가
//                String formattedContent = content.replaceAll(">([^\\s])", "><br><br>$1");
//
//                // .을 기준으로 <br> 태그 추가
//                formattedContent = formattedContent.replaceAll("\\.", ".<br><br>");
//
//                // formattedContent를 반환합니다.
//                return Optional.of(formattedContent);
//            } else {
//                return Optional.empty();
//            }
//        } catch (Exception e) {
//            logger.error("Failed to retrieve the GPT response content from S3.", e);
//            return Optional.empty();
//        }
//    }

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
//    @Scheduled(fixedDelay = 10000)  // 10초마다 실행
//    public void checkForNewFiles() {
//        String userId = "yourUserId"; // 사용자의 ID를 여기서 가져오거나 세션에서 처리
//
//        // S3에서 최신 파일을 가져옴
//        Optional<ImageInfo> latestVideo = getLatestVideo(userId);
//
//        if (latestVideo.isPresent()) {
//            String videoTimestamp = extractTimestamp(latestVideo.get().getKey());
//
//            // 기존에 처리한 파일인지 확인
//            if (!processedFiles.contains(videoTimestamp)) {
//                Optional<String> transcriptContent = getTranscriptByVideoTimestamp(userId, videoTimestamp);
//                Optional<String> gptContent = getGptResponseByVideoTimestamp(userId, videoTimestamp);
//
//                if (transcriptContent.isPresent() && !processedFiles.contains(transcriptContent.get())) {
//                    logger.info("New transcript content detected.");
//                    processedFiles.add(transcriptContent.get());
//                    // 여기서 텍스트 파일을 처리합니다 (예: 클라이언트로 전송 등)
//                }
//
//                if (gptContent.isPresent() && !processedFiles.contains(gptContent.get())) {
//                    logger.info("New GPT response content detected.");
//                    processedFiles.add(gptContent.get());
//                    // 여기서 GPT 결과를 처리합니다 (예: 클라이언트로 전송 등)
//                }
//            }
//        }
//    }

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

            // 환경 변수 설정
            Map<String, String> env = processBuilder.environment();
            env.put("AWS_ACCESS_KEY_ID", accessKey);  // 자격 증명 전달
            env.put("AWS_SECRET_ACCESS_KEY", secretKey);
            env.put("AWS_REGION", region);

            // 로그로 환경 변수 출력 (자격 증명 및 리전 정보)
            logger.info("AWS_ACCESS_KEY_ID: {}", env.get("AWS_ACCESS_KEY_ID"));
            logger.info("AWS_SECRET_ACCESS_KEY: {}", env.get("AWS_SECRET_ACCESS_KEY"));
            logger.info("AWS_REGION: {}", env.get("AWS_REGION"));

            // Python 스크립트 실행
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Python 스크립트의 출력 읽기
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

//    public String generateImageEmotionAnalysis(String s3Key, String participant, String outputPath) {
//        try {
//            // Python 명령어와 인수를 설정
//            List<String> commands = new ArrayList<>();
//            commands.add("D:\\pythonProject\\testProject\\venv\\Scripts\\python.exe"); // 가상환경의 Python 경로
//            commands.add("src/main/resources/scripts/emotionChart.py"); // Python 스크립트의 경로
//            commands.add("diaperiwinklebucket2"); // S3 버킷 이름
//            commands.add(s3Key); // S3 경로
//            commands.add(participant); // 분석할 참여자
//            commands.add(outputPath); // 결과 이미지 경로
//
//            ProcessBuilder processBuilder = new ProcessBuilder(commands);
//            processBuilder.redirectErrorStream(true);
//            Process process = processBuilder.start();
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                logger.info(line); // 스크립트 출력을 로깅합니다.
//            }
//
//            int exitCode = process.waitFor();
//            if (exitCode == 0) {
//                logger.info("Python script executed successfully.");
//                return outputPath;
//            } else {
//                logger.error("Python script execution failed with exit code: " + exitCode);
//                return null;
//            }
//        } catch (Exception e) {
//            logger.error("Failed to execute Python script for emotion analysis.", e);
//            return null;
//        }
//    }

    // 텍스트 감정 분석 수행 메서드
//    public String generateTextEmotionAnalysis(String s3Key) {
//        try {
//            // Python 명령어와 인수를 설정
//            List<String> commands = new ArrayList<>();
//            commands.add("D:\\pythonProject\\testProject\\venv\\Scripts\\python.exe"); // 가상환경의 Python 경로
//            commands.add("src/main/resources/scripts/emotionAdjust.py"); // Python 스크립트의 경로
//            commands.add("diaperiwinklebucket2"); // S3 버킷 이름
//            commands.add(s3Key); // S3 경로
//
//            ProcessBuilder processBuilder = new ProcessBuilder(commands);
//            processBuilder.redirectErrorStream(true);
//            Process process = processBuilder.start();
//
//            // 여기서 UTF-8 인코딩을 지정하여 출력을 읽음
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
//            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
//
//            StringBuilder output = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//
//            StringBuilder errorOutput = new StringBuilder();
//            while ((line = errorReader.readLine()) != null) {
//                errorOutput.append(line).append("\n");
//            }
//
//            int exitCode = process.waitFor();
//            if (exitCode == 0) {
//                logger.info("Python script executed successfully.");
//                String result = output.toString().trim();
//
//                // '.' 기준으로 줄 바꿈 처리
//                result = result.replace(".", ".<br>");
//
//                logger.info("Script output: " + result);
//                return result; // 텍스트 결과 반환
//            } else {
//                logger.error("Python script execution failed with exit code: " + exitCode);
//                logger.error("Script error output: " + errorOutput.toString());
//                return null;
//            }
//        } catch (Exception e) {
//            logger.error("Failed to execute Python script for text emotion analysis.", e);
//            return null;
//        }
//    }

    public String generateTextEmotionAnalysis(String s3Key) {
        try {
            // Python 명령어와 인수를 설정
            List<String> commands = new ArrayList<>();
            commands.add("D:\\pythonProject\\testProject\\venv\\Scripts\\python.exe"); // 가상환경의 Python 경로
            commands.add("src/main/resources/scripts/emotionAdjust.py"); // Python 스크립트의 경로
            commands.add("diaperiwinklebucket2"); // S3 버킷 이름
            commands.add(s3Key); // S3 경로

            // ProcessBuilder를 사용하여 Python 스크립트를 실행
            ProcessBuilder processBuilder = new ProcessBuilder(commands);

            // Java의 환경 변수를 Python에 전달
            Map<String, String> env = processBuilder.environment();
            env.put("AWS_ACCESS_KEY_ID", accessKey);  // 자격 증명 전달
            env.put("AWS_SECRET_ACCESS_KEY", secretKey);
            env.put("AWS_REGION", region);
            env.put("OPENAI_API_KEY", openaiApiKey);  // GPT API 키 전달 (openaiApiKey 변수가 미리 설정되어 있어야 함)

            // UTF-8 인코딩을 지정하여 출력을 읽음
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.info("Python stdout: {}", line);  // Python 스크립트 출력 로그
            }

            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
                logger.error("Python stderr: {}", line);  // Python 스크립트 에러 로그
            }

            // 파이썬 스크립트 실행 결과 처리
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Python script executed successfully.");
                String result = output.toString().trim();
                return result.replace(".", ".<br>");
            } else {
                logger.error("Python script execution failed with exit code: " + exitCode);
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

            logger.info("S3 Path Prefix: {}", s3PathPrefix);
            logger.info("Number of files found: {}", files.size());
            files.forEach(file -> logger.info("Found file: {}", file.key()));

            if (files.isEmpty()) {
                logger.warn("No files found for the specified date: {}", date);
                return Map.of("original", "No matching files found.", "result", "");
            }

            // 파일 목록 중 가장 최신의 파일 선택
            S3Object latestFile = Collections.max(files, Comparator.comparing(S3Object::lastModified));
            String latestFileKey = latestFile.key();

            logger.info("Latest file selected: {}", latestFileKey);

            // S3에서 원본 텍스트 가져오기
            String originalContent = getFileFromS3(bucketName, latestFileKey);
            logger.info("Original content to send to Python script:\n{}", originalContent);

            // Python 스크립트 실행 명령어 구성
            List<String> commands = new ArrayList<>();
            commands.add("D:\\pythonProject\\testProject\\venv\\Scripts\\python.exe");
            commands.add("src/main/resources/scripts/recommendText.py");

            logger.info("Starting Python script process...");
            commands.forEach(command -> logger.info("Command: {}", command));

            // ProcessBuilder를 사용하여 Python 스크립트 실행
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.redirectErrorStream(true);

            Map<String, String> env = processBuilder.environment();
            env.put("PYTHONIOENCODING", "UTF-8");
            env.put("OPENAI_API_KEY", openaiApiKey);

            Process process = processBuilder.start();
            logger.info("Python script process started successfully.");

            // Python 스크립트에 대화 내용 전달
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(originalContent); // 대화 내용 전달
                writer.flush();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("Python script stdout: {}", line);
                output.append(line).append("\n");
            }

            while ((line = errorReader.readLine()) != null) {
                logger.error("Python script stderr: {}", line);
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Python script executed successfully with exit code: {}", exitCode);
                logger.info("Processed result from Python:\n{}", output.toString().trim());
                return Map.of("original", originalContent, "result", output.toString().trim());
            } else {
                logger.error("Python script failed with exit code: {}", exitCode);
                return Map.of("original", "Error occurred", "result", "");
            }
        } catch (Exception e) {
            logger.error("Error during processing dialogue", e);
            return Map.of("original", "Error occurred", "result", "");
        }
    }


//    public Map<String, String> processDialogue(String userId, String date) {
//        try {
//            // S3 경로 구성
//            String s3PathPrefix = String.format("%s/done/negative_emotion_%s", userId, date);
//
//            // S3에서 해당 경로에 있는 파일 목록 가져오기
//            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
//                    .bucket(bucketName)
//                    .prefix(s3PathPrefix)
//                    .build();
//
//            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);
//            List<S3Object> files = response.contents();
//
//            logger.info("S3 Path Prefix: " + s3PathPrefix);
//            logger.info("Number of files found: " + files.size());
//            files.forEach(file -> logger.info("Found file: " + file.key()));
//
//            if (files.isEmpty()) {
//                logger.warn("No files found for the specified date: " + date);
//                return Map.of("original", "No matching files found.", "result", "");
//            }
//
//            // 파일 목록 중 가장 최신의 파일을 선택
//            S3Object latestFile = Collections.max(files, Comparator.comparing(S3Object::lastModified));
//            String latestFileKey = latestFile.key();
//
//            logger.info("Latest file selected: " + latestFileKey);
//
//            // S3에서 원본 텍스트 가져오기
//            String originalContent = getFileFromS3(bucketName, latestFileKey);
//
//            // Python 스크립트 실행 명령어 구성
//            List<String> commands = new ArrayList<>();
//            commands.add("D:\\pythonProject\\testProject\\venv\\Scripts\\python.exe"); // 가상환경의 Python 경로
//            commands.add("src/main/resources/scripts/recommendText.py"); // Python 스크립트의 경로
//
//            ProcessBuilder pb = new ProcessBuilder(commands);
//
//            // 환경 변수 설정: PYTHONIOENCODING을 UTF-8로 설정
//            Map<String, String> env = pb.environment();
//            env.put("PYTHONIOENCODING", "UTF-8");
//
//            pb.redirectErrorStream(true);
//
//            Process process = pb.start();
//
//            // 원본 텍스트를 UTF-8로 인코딩하여 파이썬 프로세스의 입력 스트림에 전달
//            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
//                writer.write(originalContent);
//                writer.flush();
//            } catch (IOException e) {
//                logger.error("Error writing to Python process", e);
//            }
//
//            // Python 스크립트의 출력을 UTF-8로 읽어들임
//            StringBuilder processedContent = new StringBuilder();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
//                String result;
//                while ((result = reader.readLine()) != null) {
//                    processedContent.append(result).append("\n");
//                }
//            } catch (IOException e) {
//                logger.error("Error reading from Python process", e);
//            }
//
//            String finalResult = processedContent.toString().trim();
//
//            // 결과를 반환할 Map 구성
//            Map<String, String> responseMap = new HashMap<>();
//            responseMap.put("original", originalContent);
//            responseMap.put("result", finalResult);
//
//            return responseMap;
//
//        } catch (Exception e) {
//            logger.error("Error during processing dialogue", e);
//            return Map.of("original", "Error occurred", "result", "");
//        }
//    }


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


    //    마이페이지 대시보드  해당 아이디에 대한 최근 이미지 파일 가져오기
    public Optional<ImageInfo> getLatestImage(String userId) {
        String userFolderPrefix = userId + "/videos/";

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userFolderPrefix)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

        return response.contents().stream()
                .filter(s -> s.key().endsWith(".jpg") || s.key().endsWith(".png")) // 이미지 파일 확장자 필터
                .max(Comparator.comparing(S3Object::lastModified))
                .map(s -> {
                    // S3의 URL을 HTTPS로 변환하여 브라우저에서 접근 가능하게 만듭니다.
                    String imageUrl = s3Client.utilities()
                            .getUrl(builder -> builder.bucket(bucketName).key(s.key()))
                            .toExternalForm(); // HTTPS URL로 변환

                    return new ImageInfo(
                            imageUrl,
                            ZonedDateTime.ofInstant(s.lastModified(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")),
                            s.size(),
                            s.key(),
                            null // GPT 제목을 원할 경우 추가할 수 있음
                    );
                });
    }


    // 메인페이지 갈등 아닐 시 S3에서 이미지, 영상 삭제
    public void deleteFiles(String timestamp) {
        try {
            String videoFileKey = "sd/videos/negative_emotion_" + timestamp + "_converted.mp4";
            String imageFileKey = "sd/videos/first_frame_" + timestamp + ".jpg";

            // S3에서 동영상 파일 삭제
            DeleteObjectRequest deleteVideoRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(videoFileKey)
                    .build();
            s3Client.deleteObject(deleteVideoRequest);
            logger.info("S3에서 동영상 파일 삭제: {}", videoFileKey);

            // S3에서 이미지 파일 삭제
            DeleteObjectRequest deleteImageRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageFileKey)
                    .build();
            s3Client.deleteObject(deleteImageRequest);
            logger.info("S3에서 이미지 파일 삭제: {}", imageFileKey);
        } catch (Exception e) {
            logger.error("S3 파일 삭제 중 오류 발생: ", e);
        }
    }


}
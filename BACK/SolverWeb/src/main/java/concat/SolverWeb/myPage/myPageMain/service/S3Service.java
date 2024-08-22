package concat.SolverWeb.myPage.myPageMain.service;

import concat.SolverWeb.myPage.myPageMain.controller.MyPageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

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


    // 특정 사용자의 폴더 내 이미지 목록 출력
    public List<ImageInfo> getAllImagesSortedByLatest(String userId) {
        String userFolderPrefix = userId + "/videos/";

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userFolderPrefix)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return response.contents().stream()
                .filter(s -> !s.key().startsWith("trash/") && (s.key().endsWith(".jpg") || s.key().endsWith(".jpeg") || s.key().endsWith(".png")))
                .sorted(Comparator.comparing(S3Object::lastModified).reversed())
                .map(s -> new ImageInfo(
                        s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s.key())).toExternalForm(),
                        ZonedDateTime.ofInstant(s.lastModified(), ZoneId.systemDefault()).format(formatter),
                        s.size(),
                        s.key()))
                .collect(Collectors.toList());
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
                .map(s -> new ImageInfo(
                        s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s.key())).toExternalForm(),
                        ZonedDateTime.ofInstant(s.lastModified(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")),
                        s.size(),
                        s.key()));
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
                .map(s -> new ImageInfo(
                        s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s.key())).toExternalForm(),
                        ZonedDateTime.ofInstant(s.lastModified(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")),
                        s.size(),
                        s.key()))
                .findFirst();
    }

    public static class ImageInfo {
        private String url;
        private String lastModifiedDate;
        private long size;
        private String key;

        public ImageInfo(String url, String lastModifiedDate, long size, String key) {
            this.url = url;
            this.lastModifiedDate = lastModifiedDate;
            this.size = size;
            this.key = key;
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

        public String getFormattedSize() {
            if (size < 1024) return size + "B";
            int exp = (int) (Math.log(size) / Math.log(1024));
            return String.format("%.1f%sB", size / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
        }
    }
}

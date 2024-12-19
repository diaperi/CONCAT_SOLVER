package concat.SolverWeb.user.coolsms.service.notice;

import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.yoonseo.repository.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class NoticeService {

    private final NoticeSmsService noticeSmsService;
    private final UserRepository userRepository;
    private final S3Client s3Client;
    private final String bucketName;

    private static final Logger logger = LoggerFactory.getLogger(NoticeService.class);

    @Autowired
    private LoggedInUserManager loggedInUserManager;

    private final Set<String> notifiedFiles = new HashSet<>(); // 알림을 보낸 파일 추적
    private Instant lastCheckedTime = Instant.now(); // 마지막 확인 시간

    @Autowired
    public NoticeService(NoticeSmsService noticeSmsService, UserRepository userRepository) {
        this.noticeSmsService = noticeSmsService;
        this.userRepository = userRepository;

        // .env 파일 또는 환경 변수에서 AWS 자격 증명과 설정값 가져오기
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String accessKey = dotenv.get("AWS_ACCESS_KEY_ID", System.getenv("AWS_ACCESS_KEY_ID"));
        String secretKey = dotenv.get("AWS_SECRET_ACCESS_KEY", System.getenv("AWS_SECRET_ACCESS_KEY"));
        String region = dotenv.get("AWS_REGION", System.getenv("AWS_REGION"));
        this.bucketName = dotenv.get("AWS_BUCKET_NAME", System.getenv("AWS_BUCKET_NAME"));

        // AWS SDK 클라이언트 생성
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Scheduled(fixedRate = 10000) // 10초마다 실행
    public void checkNewFileInS3() {
        logger.info("S3에서 파일 확인 시작");

        try {
            // 현재 로그인된 사용자 ID 확인
            String loggedInUserId = getLoggedInUserId();
            if (loggedInUserId == null) {
                logger.warn("로그인된 사용자가 없습니다.");
                return;
            }

            // 사용자 정보 조회
            Optional<UserEntity> userOptional = userRepository.findByUserId(loggedInUserId);
            if (userOptional.isEmpty()) {
                logger.warn("사용자가 존재하지 않습니다. userId={}", loggedInUserId);
                return;
            }

            UserEntity user = userOptional.get();
            String userId = user.getUserId();
            logger.info("로그인된 사용자 ID: {}", userId);

            // 사용자 폴더 경로 생성
            String userFolderPrefix = userId + "/gpt/";

            // S3에서 새 파일 확인
            if (checkForNewFilesInS3(userFolderPrefix)) {
                logger.info("새로운 파일이 있습니다. {}님에게 알림을 보냅니다.", userId);

                // 인증 상태와 전화번호 확인 후 SMS 발송
                String userPhone = user.getUserPhone();
                if ("Y".equals(user.getIsVerified()) && userPhone != null) {
                    noticeSmsService.checkNewFileInS3(userPhone); // 문자 발송
                } else {
                    logger.warn("사용자 인증 상태가 유효하지 않거나 전화번호가 없습니다.");
                }
            } else {
                logger.info("새로운 파일이 없습니다.");
            }
        } catch (Exception e) {
            // 예외 처리
            logger.error("Scheduled task에서 오류 발생: {}", e.getMessage(), e);
        }
    }

    // S3에서 새로운 파일 확인
    public boolean checkForNewFilesInS3(String userFolderPrefix) {
        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(userFolderPrefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);

            boolean newFileFound = false;

            for (S3Object s3Object : response.contents()) {
                String fileKey = s3Object.key();
                Instant lastModified = s3Object.lastModified();

                // 마지막 확인 시간 이후에 수정된 파일인지 확인
                if (lastModified.isAfter(lastCheckedTime) && notifiedFiles.add(fileKey)) {
                    logger.info("새로운 파일 발견: {}", fileKey);
                    newFileFound = true;
                }
            }

            // 마지막 확인 시간 갱신
            lastCheckedTime = Instant.now();
            return newFileFound;
        } catch (Exception e) {
            logger.error("S3에서 파일 확인 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    private String getLoggedInUserId() {
        return loggedInUserManager.getLoggedInUserId();
    }

}

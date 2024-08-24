package concat.SolverWeb.myPage.trashCs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class TrashService {

    private static final Logger logger = LoggerFactory.getLogger(TrashService.class);

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public TrashService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public boolean moveToTrash(String videoUrl) {
        try {
            // 비디오 URL에서 파일 이름 추출
            String key = videoUrl.substring(videoUrl.lastIndexOf('/') + 1);

            // 숫자 부분 추출
            String numberPart = key.replaceAll(".*_(\\d{8}_\\d{6}).*", "$1");

            if (numberPart.isEmpty()) {
                throw new RuntimeException("URL에서 숫자 부분을 추출할 수 없습니다: " + key);
            }

            String imageKey = "first_frame_" + numberPart + ".jpg";
            String trashKey = "trash/" + key;
            String trashImageKey = "trash/" + imageKey;

            // 비디오 존재 여부
            boolean videoExists = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build()) != null;

            if (!videoExists) {
                throw new RuntimeException("Video does not exist: " + key);
            }

            // 비디오를 trash 폴더로 복사
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(key)
                    .destinationBucket(bucketName)
                    .destinationKey(trashKey)
                    .build());
            logger.info("Video copied successfully: {}", trashKey);

            // 원본 비디오 삭제
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            logger.info("Video deleted successfully: {}", key);

            // 이미지 복사
            boolean imageExists = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageKey)
                    .build()) != null;

            if (imageExists) {
                s3Client.copyObject(CopyObjectRequest.builder()
                        .sourceBucket(bucketName)
                        .sourceKey(imageKey)
                        .destinationBucket(bucketName)
                        .destinationKey(trashImageKey)
                        .build());
                logger.info("Image copied successfully: {}", trashImageKey);

                // 원본 이미지 삭제
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(imageKey)
                        .build());
                logger.info("Image deleted successfully: {}", imageKey);
            } else {
                logger.warn("Image does not exist: {}", imageKey);
            }
            return true; // 파일 이동 및 삭제 성공
        } catch (Exception e) {
            logger.error("Move and delete failed", e);
            return false; // 파일 이동 및 삭제 실패
        }
    }
}

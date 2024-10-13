package concat.SolverWeb.myPage.trashCs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

@Service
public class TrashService {

    private static final Logger logger = LoggerFactory.getLogger(TrashService.class);

    private final S3Client s3Client;

    @Value("diaperiwinklebucket2")
    private String bucketName;

    public TrashService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public boolean moveToTrash(String videoUrl, String userId) {
        try {
            // 비디오 URL에서 파일 이름과 타임스탬프 추출
            String key = videoUrl.substring(videoUrl.lastIndexOf('/') + 1);  // 파일 이름 추출
            String timestamp = key.replaceAll(".*_(\\d{8}_\\d{6}).*", "$1");

            if (timestamp.isEmpty()) {
                throw new RuntimeException("URL에서 타임스탬프를 추출할 수 없습니다: " + key);
            }

            String videoKey = userId + "/videos/" + key;
            String imageKey = userId + "/videos/first_frame_" + timestamp + ".jpg";
            String trashKey = userId + "/trash/" + key;
            String trashImageKey = userId + "/trash/first_frame_" + timestamp + ".jpg";

            // 비디오 처리
            boolean videoMoved = moveVideoToTrash(videoKey, trashKey);
            if (!videoMoved) {
                return false;
            }

            // 이미지 처리
            boolean imageMoved = moveImageToTrash(imageKey, trashImageKey);
            if (!imageMoved) {
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("Move and delete failed", e);
            return false;
        }
    }

    private boolean moveVideoToTrash(String videoKey, String trashKey) {
        try {
            // 비디오 존재 여부 확인
            boolean videoExists = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(videoKey)
                    .build()).sdkHttpResponse().isSuccessful();

            if (!videoExists) {
                logger.error("Video does not exist: {}", videoKey);
                return false;
            }

            // 비디오를 trash 폴더로 복사
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(videoKey)
                    .destinationBucket(bucketName)
                    .destinationKey(trashKey)
                    .build());

            // 원본 비디오 삭제
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(videoKey)
                    .build());

            return true;
        } catch (Exception e) {
            logger.error("Failed to move and delete video: {}", videoKey, e);
            return false;
        }
    }

    private boolean moveImageToTrash(String imageKey, String trashImageKey) {
        try {
            // 이미지 존재 여부 확인
            boolean imageExists = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageKey)
                    .build()).sdkHttpResponse().isSuccessful();

            if (imageExists) {
                // 이미지를 trash 폴더로 복사
                s3Client.copyObject(CopyObjectRequest.builder()
                        .sourceBucket(bucketName)
                        .sourceKey(imageKey)
                        .destinationBucket(bucketName)
                        .destinationKey(trashImageKey)
                        .build());

                // 원본 이미지 삭제
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(imageKey)
                        .build());
                return true;
            } else {
                logger.warn("Image does not exist: {}", imageKey);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to move and delete image: {}", imageKey, e);
            return false;
        }
    }
}

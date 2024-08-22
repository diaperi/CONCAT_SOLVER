package concat.SolverWeb.myPage.trashCs.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import concat.SolverWeb.myPage.myPageMain.service.S3Service;
import concat.SolverWeb.user.email.service.VerifyEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrashService {

    private static final Logger logger = LoggerFactory.getLogger(VerifyEmailService.class);

    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public TrashService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public boolean moveToTrash(String videoUrl) {
        try {
            // 비디오 url에서 파일 이름 추출
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
            if (!s3Client.doesObjectExist(bucketName, key)) {
                throw new RuntimeException("Video does not exist: " + key);
            }

            // 비디오를 trash 폴더로 복사
            CopyObjectRequest copyObjRequest = new CopyObjectRequest(bucketName, key, bucketName, trashKey);
            s3Client.copyObject(copyObjRequest);
            logger.info("Video copied successfully: {}", trashKey);


            // 원본 비디오 삭제
            DeleteObjectRequest deleteObjRequest = new DeleteObjectRequest(bucketName, key);
            s3Client.deleteObject(deleteObjRequest);
            logger.info("Video deleted successfully: {}", key);

            // 이미지 복사
            if (s3Client.doesObjectExist(bucketName, imageKey)) {
                CopyObjectRequest copyImageObjRequest = new CopyObjectRequest(bucketName, imageKey, bucketName, trashImageKey);
                s3Client.copyObject(copyImageObjRequest);
                logger.info("Image copied successfully: {}", trashImageKey);

                // 원본 이미지 삭제
                DeleteObjectRequest deleteImageObjRequest = new DeleteObjectRequest(bucketName, imageKey);
                s3Client.deleteObject(deleteImageObjRequest);
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

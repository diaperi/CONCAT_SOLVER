package concat.SolverWeb.myPage.trashCs.service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class S3CleanupService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private static final String TRASH_FOLDER = "trash/";
    private static final long EXPIRATION_DAYS = 30L;

    public S3CleanupService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldFiles() {
        ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(TRASH_FOLDER)
                .build();

        try {
            ListObjectsV2Response result = s3Client.listObjectsV2(req);

            for (var s3Object : result.contents()) {
                String key = s3Object.key();
                Instant lastModified = s3Object.lastModified();

                if (isOlderThanDays(lastModified, EXPIRATION_DAYS)) {
                    try {
                        s3Client.deleteObject(DeleteObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build());
                    } catch (S3Exception e) {
                        System.err.println("Error deleting file " + key + ": " + e.getMessage());
                    }
                }
            }
        } catch (S3Exception e) {
            System.err.println("Error listing objects from bucket " + bucketName + ": " + e.getMessage());
        }
    }

    private boolean isOlderThanDays(Instant lastModified, long days) {
        ZonedDateTime expirationDate = ZonedDateTime.now().minusDays(days);
        return ZonedDateTime.ofInstant(lastModified, ZoneId.systemDefault()).isBefore(expirationDate);
    }
}

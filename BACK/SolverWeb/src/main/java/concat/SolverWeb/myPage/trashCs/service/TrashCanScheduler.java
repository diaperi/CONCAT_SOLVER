package concat.SolverWeb.myPage.trashCs.service;

import concat.SolverWeb.user.yoonseo.entity.UserEntity;
import concat.SolverWeb.user.yoonseo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrashCanScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TrashCanScheduler.class);

    private final UserRepository userRepository;
    private final S3Client s3Client;

    @Value("diaperiwinklebucket2")
    private String bucketName;

    public TrashCanScheduler(UserRepository userRepository, S3Client s3Client) {
        this.userRepository = userRepository;
        this.s3Client = s3Client;
    }

    // 매일 자정에 실행
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldTrashVideosForAllUsers() {
        try {
            List<UserEntity> allUsers = userRepository.findAll();
            for (UserEntity user : allUsers) {
                String userId = user.getUserId();
                deleteOldTrashVideosForUser(userId);
            }
        } catch (Exception e) {
            logger.error("스케줄러를 통한 영상 삭제 실패", e);
        }
    }

    // 오래된 영상 삭제
    private void deleteOldTrashVideosForUser(String userId) {
        String trashPrefix = getTrashPrefix(userId);

        try {
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

            ListObjectsV2Request listObjects = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(trashPrefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjects);
            List<S3Object> allObjects = response.contents();

            List<S3Object> oldObjects = allObjects.stream()
                    .filter(s3Object -> {
                        String key = s3Object.key();
                        return key.endsWith(".jpg") || key.endsWith(".jpeg") || key.endsWith(".mp4");
                    })
                    .filter(s3Object -> {
                        LocalDateTime objectDateTime = Instant.ofEpochMilli(s3Object.lastModified().toEpochMilli())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        // 마지막 수정일로부터 30일 후 삭제
                        LocalDateTime deleteByDateTime = objectDateTime.plusDays(30);
                        return now.isAfter(deleteByDateTime);
                    })
                    .collect(Collectors.toList());

            // 삭제 처리
            if (!oldObjects.isEmpty()) {
                final int MAX_KEYS = 1000;
                for (int i = 0; i < oldObjects.size(); i += MAX_KEYS) {
                    List<ObjectIdentifier> objectIdentifiers = oldObjects.subList(i, Math.min(i + MAX_KEYS, oldObjects.size())).stream()
                            .map(s3Object -> ObjectIdentifier.builder()
                                    .key(s3Object.key())
                                    .build())
                            .collect(Collectors.toList());

                    DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                            .bucket(bucketName)
                            .delete(d -> d.objects(objectIdentifiers))
                            .build();

                    DeleteObjectsResponse deleteResponse = s3Client.deleteObjects(deleteRequest);
                    logger.info("오래된 영상을 삭제했습니다. {}개", deleteResponse.deleted().size() - 1);
                }
            } else {
                logger.info("삭제할 오래된 영상이 없습니다.");
            }
        } catch (Exception e) {
            logger.error("오래된 영상 삭제 실패", e);
        }
    }

    private String getTrashPrefix(String userId) {
        return userId + "/trash/";
    }
}

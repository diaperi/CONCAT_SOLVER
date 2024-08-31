package concat.SolverWeb.myPage.trashCs.service;

import concat.SolverWeb.user.yoonseo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TrashCanService {

    private static final Logger logger = LoggerFactory.getLogger(TrashCanService.class);

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public TrashCanService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private String getTrashPrefix(String userId) {
        return userId + "/trash/";
    }

    // 날짜별 분류
    public Map<String, Object> getTrashVideosByDate(String userId) {
        Map<String, Object> categorizedVideos = new HashMap<>();
        String trashPrefix = getTrashPrefix(userId);

        try {
            ListObjectsV2Request listObjects = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(trashPrefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjects);

            List<S3Object> sortedObjects = response.contents().stream()
                    .filter(s3Object -> s3Object.key().endsWith(".jpg") || s3Object.key().endsWith(".jpeg"))
                    .sorted(Comparator.comparing(S3Object::lastModified).reversed())
                    .collect(Collectors.toList());

            LocalDate now = LocalDate.now();
            LocalDate startOfWeek = now.with(java.time.DayOfWeek.MONDAY);
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            LocalDate startOfLastWeek = startOfWeek.minusWeeks(1);
            LocalDate endOfLastWeek = startOfLastWeek.plusDays(6);

            LocalDate startOf2WeeksAgo = startOfLastWeek.minusWeeks(1);
            LocalDate endOf2WeeksAgo = startOf2WeeksAgo.plusDays(6);

            LocalDate startOf3WeeksAgo = startOf2WeeksAgo.minusWeeks(1);
            LocalDate endOf3WeeksAgo = startOf3WeeksAgo.plusDays(6);

            LocalDate startOf4WeeksAgo = startOf3WeeksAgo.minusWeeks(1);
            LocalDate endOf4WeeksAgo = startOf4WeeksAgo.plusDays(6);

            // 분류된 영상 리스트를 날짜별로 정리
            List<String> todayVideos = getVideosByDate(sortedObjects, now, now);
            List<String> thisWeekVideos = getVideosByDate(sortedObjects, startOfWeek, endOfWeek);
            List<String> last1WeekVideos = getVideosByDate(sortedObjects, startOfLastWeek, endOfLastWeek);
            List<String> last2WeekVideos = getVideosByDate(sortedObjects, startOf2WeeksAgo, endOf2WeeksAgo);
            List<String> last3WeeksVideos = getVideosByDate(sortedObjects, startOf3WeeksAgo, endOf3WeeksAgo);
            List<String> last4WeeksVideos = getVideosByDate(sortedObjects, startOf4WeeksAgo, endOf4WeeksAgo);

            // 오늘 영상은 이번 주 리스트에서 제외
            List<String> thisWeekVideosExcludingToday = thisWeekVideos.stream()
                    .filter(video -> !todayVideos.contains(video))
                    .collect(Collectors.toList());

            categorizedVideos.put("today", todayVideos);
            categorizedVideos.put("thisWeek", thisWeekVideosExcludingToday);
            categorizedVideos.put("last1Week", last1WeekVideos);
            categorizedVideos.put("last2Week", last2WeekVideos);
            categorizedVideos.put("last3Weeks", last3WeeksVideos);
            categorizedVideos.put("last4Weeks", last4WeeksVideos);

            // logger.info("Classify the video list by date"); // 영상을 날짜별 분류
        } catch (Exception e) {
            logger.error("trash 영상 목록 가져오기 실패", e);
        }
        return categorizedVideos;
    }

    private List<String> getVideosByDate(List<S3Object> objects, LocalDate startDate, LocalDate endDate) {
        return objects.stream()
                .filter(s3Object -> {
                    LocalDate objectDate = Instant.ofEpochMilli(s3Object.lastModified().toEpochMilli())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return !objectDate.isBefore(startDate) && !objectDate.isAfter(endDate);
                })
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    // 휴지통 비우기
    public void deleteAllTrashVideos(String userId) {
        String trashPrefix = getTrashPrefix(userId);

        try {
            ListObjectsV2Request listObjects = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(trashPrefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjects);

            List<S3Object> allObjects = response.contents();

            Map<String, List<S3Object>> groupedByTimestamp = allObjects.stream()
                    .filter(s3Object -> s3Object.key().endsWith(".jpg") || s3Object.key().endsWith(".jpeg") || s3Object.key().endsWith(".mp4"))
                    .collect(Collectors.groupingBy(s3Object -> {
                        String key = s3Object.key();
                        return key.replaceAll(".*_(\\d{8}_\\d{6}).*", "$1");
                    }));

            // 삭제 처리
            groupedByTimestamp.forEach((timestamp, objectsToDelete) -> {
                try {
                    if (!objectsToDelete.isEmpty()) {
                        final int MAX_KEYS = 1000;
                        for (int i = 0; i < objectsToDelete.size(); i += MAX_KEYS) {
                            List<ObjectIdentifier> objectIdentifiers = objectsToDelete.subList(i, Math.min(i + MAX_KEYS, objectsToDelete.size())).stream()
                                    .map(s3Object -> ObjectIdentifier.builder()
                                            .key(s3Object.key())
                                            .build())
                                    .toList();

                            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                                    .bucket(bucketName)
                                    .delete(d -> d.objects(objectIdentifiers))
                                    .build();

                            DeleteObjectsResponse deleteResponse = s3Client.deleteObjects(deleteRequest);
                            logger.info("휴지통 비우기를 성공했습니다. {}개", deleteResponse.deleted().size() - 1);
                        }
                    }
                } catch (Exception e) {
                    logger.error("휴지통 비우기 중 오류 발생", e);
                }
            });

        } catch (Exception e) {
            logger.error("휴지통 비우기 실패", e);
        }
    }

    // 휴지통 영상 복구
    public boolean recoverVideo(String videoKey, String userId) {
        String trashPrefix = getTrashPrefix(userId);

        try {
            String timestamp = videoKey.replaceAll(".*_(\\d{8}_\\d{6}).*", "$1");
            String sourceVideoKey = userId + "/trash/" + videoKey;
            String destinationVideoKey = userId + "/videos/" + videoKey.substring(videoKey.lastIndexOf("/") + 1);

            String sourceImageKey = userId + "/trash/first_frame_" + timestamp + ".jpg";
            String destinationImageKey = userId + "/videos/first_frame_" + timestamp + ".jpg";

            // 비디오 복사
            CopyObjectRequest videoCopyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceVideoKey)
                    .destinationBucket(bucketName)
                    .destinationKey(destinationVideoKey)
                    .build();
            s3Client.copyObject(videoCopyRequest);
            // logger.info("Successfully copied video");

            // 이미지 복사
            CopyObjectRequest imageCopyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceImageKey)
                    .destinationBucket(bucketName)
                    .destinationKey(destinationImageKey)
                    .build();
            s3Client.copyObject(imageCopyRequest);
            // logger.info("Successfully copied image");

            // mp4 파일 찾기
            ListObjectsV2Request listObjects = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(trashPrefix)
                    .build();
            ListObjectsV2Response response = s3Client.listObjectsV2(listObjects);

            List<S3Object> relatedMp4Files = response.contents().stream()
                    .filter(s3Object -> s3Object.key().endsWith(".mp4") && s3Object.key().contains(timestamp))
                    .toList();

            // mp4 파일 복사 및 삭제
            for (S3Object mp4File : relatedMp4Files) {
                String mp4SourceKey = mp4File.key();
                String mp4DestinationKey = userId + "/videos/" + mp4SourceKey.substring(mp4SourceKey.lastIndexOf("/") + 1);

                // mp4 복사
                CopyObjectRequest mp4CopyRequest = CopyObjectRequest.builder()
                        .sourceBucket(bucketName)
                        .sourceKey(mp4SourceKey)
                        .destinationBucket(bucketName)
                        .destinationKey(mp4DestinationKey)
                        .build();
                s3Client.copyObject(mp4CopyRequest);
                // logger.info("Successfully copied related video");

                // mp4 원본 삭제
                DeleteObjectRequest mp4DeleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(mp4SourceKey)
                        .build();
                s3Client.deleteObject(mp4DeleteRequest);
                // logger.info("Successfully deleted related video");
            }

            // 원본 mp4 삭제
            DeleteObjectRequest deleteVideoRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(sourceVideoKey)
                    .build();
            s3Client.deleteObject(deleteVideoRequest);
            // logger.info("Successfully deleted video");

            // 원본 이미지 삭제
            DeleteObjectRequest deleteImageRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(sourceImageKey)
                    .build();
            s3Client.deleteObject(deleteImageRequest);
            // logger.info("Successfully deleted image");

            return true;
        } catch (Exception e) {
            logger.error("Failed to recover video", e);
            return false;
        }
    }

    // GPT 제목
    public String getGptTitle(String userId, String imageKey) {
        try {
            String timestamp = imageKey.replaceAll(".*_(\\d{8}_\\d{6}).*", "$1");
            if (timestamp.isEmpty()) {
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

                try (ResponseInputStream<GetObjectResponse> s3ObjectInputStream = s3Client.getObject(getObjectRequest);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(s3ObjectInputStream))) {

                    String content = reader.lines().collect(Collectors.joining("\n"));
                    Matcher matcher = Pattern.compile("<(.*?)>").matcher(content);

                    if (matcher.find()) {
                        return matcher.group(1); // 제목 반환
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve GPT title from S3.", e);
        }

        return "제목 없음"; // 예외 발생 시 기본 제목 반환
    }
}
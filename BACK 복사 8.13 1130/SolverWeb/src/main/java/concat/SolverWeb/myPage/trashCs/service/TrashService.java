package concat.SolverWeb.myPage.trashCs.service;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import concat.SolverWeb.myPage.trashCs.dto.TrashItem;

@Service
public class TrashService {

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
                .endpointOverride(URI.create("https://s3.ap-southeast-2.amazonaws.com"))
                .build();
    }

    public void moveFilesToTrash(String commonTimestamp) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

        List<S3Object> objectsToMove = response.contents().stream()
                .filter(s -> s.key().contains(commonTimestamp) &&
                        (s.key().endsWith(".mp4") || s.key().endsWith(".jpg")))
                .collect(Collectors.toList());

        for (S3Object s3Object : objectsToMove) {
            String sourceKey = s3Object.key();
            String destinationKey = "trash/" + sourceKey;

            try {
                CopyObjectRequest copyReq = CopyObjectRequest.builder()
                        .copySource(bucketName + "/" + sourceKey)
                        .destinationBucket(bucketName)
                        .destinationKey(destinationKey)
                        .build();
                s3Client.copyObject(copyReq);

                DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(sourceKey)
                        .build();
                s3Client.deleteObject(deleteReq);

            } catch (S3Exception e) {
                System.err.println("Error processing file " + s3Object.key() + ": " + e.getMessage());
            }
        }
    }

    public void deleteFile(String fileName) {
        String key = "trash/" + fileName;

        try {
            DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteReq);

            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix("trash/")
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);
            boolean fileStillExists = response.contents().stream()
                    .anyMatch(s3Object -> s3Object.key().equals(key));

            if (!fileStillExists) {
                System.out.println("File successfully deleted: " + key);
            } else {
                System.out.println("File still exists after delete request: " + key);
            }

        } catch (S3Exception e) {
            System.err.println("Error deleting file " + key + ": " + e.getMessage());
        }
    }

    public static String extractCommonTimestamp(String fileName) {
        Pattern pattern = Pattern.compile("\\d{8}_\\d{6}");
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            return matcher.group(0);
        }

        return null;
    }

    public List<TrashItem> getItemsForToday() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        return listTrashFiles().stream()
                .filter(item -> {
                    Instant itemInstant = item.getLastModified();
                    LocalDateTime itemDate = LocalDateTime.ofInstant(itemInstant, ZoneId.systemDefault());
                    return itemDate.isAfter(todayStart) && itemDate.isBefore(todayEnd);
                })
                .collect(Collectors.toList());
    }

    public List<TrashItem> getItemsForThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfWeek = getStartOfWeek(today);
        LocalDateTime endOfWeek = startOfWeek.plusWeeks(1).minusNanos(1);

        return listTrashFiles().stream()
                .filter(item -> {
                    Instant itemInstant = item.getLastModified();
                    LocalDateTime itemDate = LocalDateTime.ofInstant(itemInstant, ZoneId.systemDefault());
                    return itemDate.isAfter(startOfWeek) && itemDate.isBefore(endOfWeek);
                })
                .collect(Collectors.toList());
    }

    public List<TrashItem> getItemsForLastWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfLastWeek = getStartOfWeek(today).minusWeeks(1).toLocalDate();
        LocalDateTime startOfLastWeekStart = getStartOfWeek(startOfLastWeek);
        LocalDateTime endOfLastWeek = startOfLastWeekStart.plusWeeks(1).minusNanos(1);

        return listTrashFiles().stream()
                .filter(item -> {
                    Instant itemInstant = item.getLastModified();
                    LocalDateTime itemDate = LocalDateTime.ofInstant(itemInstant, ZoneId.systemDefault());
                    return itemDate.isAfter(startOfLastWeekStart) && itemDate.isBefore(endOfLastWeek);
                })
                .collect(Collectors.toList());
    }

    public List<TrashItem> getItemsForThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = getStartOfMonth(today);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        return listTrashFiles().stream()
                .filter(item -> {
                    Instant itemInstant = item.getLastModified();
                    LocalDateTime itemDate = LocalDateTime.ofInstant(itemInstant, ZoneId.systemDefault());
                    return itemDate.isAfter(startOfMonth) && itemDate.isBefore(endOfMonth);
                })
                .collect(Collectors.toList());
    }

    private LocalDateTime getStartOfWeek(LocalDate date) {
        return date.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).atStartOfDay();
    }

    private LocalDateTime getStartOfMonth(LocalDate date) {
        return date.withDayOfMonth(1).atStartOfDay();
    }

    public List<TrashItem> listTrashFiles() {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("trash/")
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

        return response.contents().stream()
                .map(s3Object -> new TrashItem(
                        s3Object.key(),
                        "URL for " + s3Object.key(),
                        s3Object.lastModified()))
                .collect(Collectors.toList());
    }
}

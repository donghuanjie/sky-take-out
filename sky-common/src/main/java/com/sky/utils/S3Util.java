package com.sky.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class S3Util {
    private String region;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, accessKeySecret);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCredentials);

        S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();

        String fileUrl = null;
        try {
            // 创建 PutObject 请求并上传文件
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectName)
                            .build(),
                    RequestBody.fromBytes(bytes));

            // 生成标准的虚拟主机样式 URL
            fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectName);
            log.info("文件上传成功，访问 URL: {}", fileUrl);

        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage());
            e.printStackTrace();
        } finally {
            s3Client.close();
        }

        return fileUrl;
    }
}

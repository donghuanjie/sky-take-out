package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.s3")
@Data
public class S3Properties {

    private String region;
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;
}

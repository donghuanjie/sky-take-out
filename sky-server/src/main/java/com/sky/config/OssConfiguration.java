package com.sky.config;

import com.sky.properties.S3Properties;
import com.sky.utils.S3Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于创建 Util 对象
 */
@Configuration
@Slf4j
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public S3Util s3Util(S3Properties s3Properties) {
        log.info("创建Amazon S3文件上传工具类对象: {}", s3Properties);
        return new S3Util(s3Properties.getRegion(),
                s3Properties.getAccessKeyId(),
                s3Properties.getAccessKeySecret(),
                s3Properties.getBucketName());
    }
}

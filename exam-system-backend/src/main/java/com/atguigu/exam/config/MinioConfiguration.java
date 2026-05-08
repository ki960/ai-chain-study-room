package com.atguigu.exam.config;

import com.atguigu.exam.config.properties.MinioProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfiguration {
    @Autowired
    private MinioProperties minioProperties;

    @Bean
    public MinioClient getMinioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        log.info("Minio文件服务器连接成功，信息为:{}", minioClient);
        return minioClient;
    }
}

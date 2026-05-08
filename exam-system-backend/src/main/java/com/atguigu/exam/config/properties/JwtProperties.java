package com.atguigu.exam.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String secretKey;
    private long ttlMillis;
    private String tokenName;
}

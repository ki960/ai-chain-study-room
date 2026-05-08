package com.atguigu.exam.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "doubao")
public class DoubaoProperties {
    private String model;
    private String baseUrl;
    private String apiKey;
    private Integer maxTokens;
    private Double temperature;
}

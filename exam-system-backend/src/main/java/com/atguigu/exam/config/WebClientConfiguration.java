package com.atguigu.exam.config;

import com.atguigu.exam.config.properties.DoubaoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Deleted:import org.springframework.context.annotation.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
@EnableConfigurationProperties(DoubaoProperties.class)
public class WebClientConfiguration {

    @Autowired
    private DoubaoProperties doubaoProperties;

    @Bean
    public WebClient doubaoWebClient() {
        System.out.println("===== 注入的BaseUrl：" + doubaoProperties.getBaseUrl() + " =====");
        return WebClient.builder()
                .baseUrl(doubaoProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + doubaoProperties.getApiKey())
                .build();
    }
}

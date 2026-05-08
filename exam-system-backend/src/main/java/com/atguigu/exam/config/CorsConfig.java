package com.atguigu.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域配置
 * 解决前后端分离开发中的跨域问题
 */
@Configuration
public class CorsConfig {

    /**
     * 配置跨域过滤器
     * @return CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源地址（开发环境允许所有来源）
        config.addAllowedOriginPattern("*");
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 允许的HTTP方法
        config.addAllowedMethod("*");
        
        // 是否允许发送Cookie
        config.setAllowCredentials(true);
        
        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);
        
        // 暴露的响应头
        config.addExposedHeader("Content-Disposition");
        config.addExposedHeader("Authorization");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}

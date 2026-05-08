package com.atguigu.exam.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

public class JwtUtils {

    /**
     * 生成JWT Token
     */
    public static String createJWt(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 生成安全密钥（新版API要求）
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .signWith(key) // 新版API简化
                .setExpiration(exp);

        return builder.compact();
    }

    /**
     * 解析JWT Token
     */
    public static Claims parseJWT(String secretKey, String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.parserBuilder()
                .setSigningKey(key) // 新版API
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
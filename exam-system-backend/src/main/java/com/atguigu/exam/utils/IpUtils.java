package com.atguigu.exam.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP工具类
 * 用于获取客户端真实IP地址、判断内网IP、IP格式校验
 */
public class IpUtils {

    /**
     * 获取客户端真实IP地址（适配Nginx/网关/多级反向代理）
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = null;
        // 按常用代理请求头依次获取真实IP
        String[] headerKeys = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };
        for (String header : headerKeys) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                break;
            }
        }
        // 所有代理头都取不到，使用原始请求地址
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理：X-Forwarded-For=客户端IP,代理1,代理2
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        // 统一本地IPv6地址转为IPv4本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    /**
     * 校验IP是否有效（非空、非unknown）
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 检查IP地址是否为内网IP
     * 内网段：127.0.0.0/8、10.0.0.0/8、172.16.0.0/12、192.168.0.0/16
     * @param ip IP地址
     * @return true 内网IP / false 外网IP
     */
    public static boolean isInternalIp(String ip) {
        if (!isValidIp(ip) || !isIpv4(ip)) {
            return false;
        }
        // 本地回环地址
        if (ip.startsWith("127.")) {
            return true;
        }
        try {
            String[] parts = ip.split("\\.");
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            // 10.0.0.0/8
            if (first == 10) {
                return true;
            }
            // 172.16.0.0 ~ 172.31.255.255
            if (first == 172 && second >= 16 && second <= 31) {
                return true;
            }
            // 192.168.0.0/16
            if (first == 192 && second == 168) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 简单校验是否为合法IPv4格式
     */
    public static boolean isIpv4(String ip) {
        if (!isValidIp(ip)) {
            return false;
        }
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
package com.atguigu.exam.interceptor;

import com.atguigu.exam.config.properties.JwtProperties;
import com.atguigu.exam.constant.JwtClaimsConstant;
import com.atguigu.exam.context.BaseContext;
import com.atguigu.exam.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        if(!(handler instanceof HandlerMethod))
            return true;
        String token = request.getHeader(jwtProperties.getTokenName());

        try{
            log.info("JWT校验:{}",token);
            Claims claims = JwtUtils.parseJWT( jwtProperties.getSecretKey(),token);

            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户ID:{}",userId);
            BaseContext.setCurrentId(userId);

            return true;
        }catch (Exception e){
            log.error("JWT校验失败:{}",e.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后，清理 ThreadLocal (防止内存泄漏)
        BaseContext.removeCurrentId();
    }
}

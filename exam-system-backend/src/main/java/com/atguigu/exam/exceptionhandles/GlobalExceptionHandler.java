package com.atguigu.exam.exceptionhandles;

import com.atguigu.exam.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e) {
        e.printStackTrace();
        log.error("异常信息为:{}", e.getMessage());
        return Result.error(e.getMessage());
    }
}

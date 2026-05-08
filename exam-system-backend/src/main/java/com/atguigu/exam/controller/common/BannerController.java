package com.atguigu.exam.controller.common;


import com.atguigu.exam.common.Result;
import com.atguigu.exam.entity.Banner;
import com.atguigu.exam.service.BannerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.minio.errors.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 轮播图控制器 - 处理轮播图管理相关的HTTP请求
 * 包括图片上传、轮播图的CRUD操作、状态切换等功能
 */
@RestController  // REST控制器，返回JSON数据
@RequestMapping("/api/common/banners")  // 轮播图API路径前缀
@CrossOrigin  // 允许跨域访问
@Slf4j
@Tag(name = "轮播图管理", description = "轮播图相关操作，包括图片上传、轮播图增删改查、状态管理等功能")  // Swagger API分组
public class BannerController {

    @Autowired
    private BannerService bannerService;
    /**
     * 获取启用的轮播图（前台首页使用）
     * @return 轮播图列表
     */
    @GetMapping("/active")  // 处理GET请求
    @Operation(summary = "获取启用的轮播图", description = "获取状态为启用的轮播图列表，供前台首页展示使用")  // API描述
    public Result<List<Banner>> getActiveBanners() {
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Banner::getIsActive, true);
        queryWrapper.orderByAsc(Banner::getSortOrder);
        List<Banner> bannerList = bannerService.list(queryWrapper);
        log.info("获取启用的轮播图,{}", bannerList);
        return Result.success(bannerList);
    }

} 
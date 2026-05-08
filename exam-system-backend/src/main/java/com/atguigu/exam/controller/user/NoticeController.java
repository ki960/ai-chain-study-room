package com.atguigu.exam.controller.user;

import com.atguigu.exam.common.Result;
import com.atguigu.exam.entity.Notice;
import com.atguigu.exam.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器 - 处理系统公告管理相关的HTTP请求
 * 包括公告的增删改查、状态管理、前台展示等功能
 */
@RestController  // REST控制器，返回JSON数据
@RequestMapping("/api/user/notices")  // 公告API路径前缀
@CrossOrigin  // 允许跨域访问
@Tag(name = "公告管理", description = "系统公告相关操作，包括公告发布、编辑、删除、状态管理等功能")  // Swagger API分组
public class NoticeController {
    
    /**
     * 注入公告业务服务
     */
    @Autowired
    private NoticeService noticeService;
    
    /**
     * 获取启用的公告（前台首页使用）
     * @return 公告列表
     */
    @GetMapping("/active")  // 处理GET请求
    @Operation(summary = "获取启用的公告", description = "获取状态为启用的公告列表，供前台首页展示使用")  // API描述
    public Result<List<Notice>> getActiveNotices() {
        return noticeService.getActiveNotices();
    }
    
    /**
     * 获取最新的几条公告（前台首页使用）
     * @param limit 限制数量，默认5条
     * @return 公告列表
     */
    @GetMapping("/latest")  // 处理GET请求
    @Operation(summary = "获取最新公告", description = "获取最新发布的公告列表，用于首页推荐展示")  // API描述
    public Result<List<Notice>> getLatestNotices(
            @Parameter(description = "限制数量", example = "5") @RequestParam(defaultValue = "5") int limit) {
        return noticeService.getLatestNotices(limit);
    }
} 
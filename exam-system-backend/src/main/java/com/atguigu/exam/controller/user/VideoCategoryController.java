package com.atguigu.exam.controller.user;


import com.atguigu.exam.common.Result;
import com.atguigu.exam.entity.VideoCategory;
import com.atguigu.exam.service.VideoCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频分类控制器
 * 处理视频分类管理相关的HTTP请求
 */
@RestController
@RequestMapping("/api/user/video-categories")
@Tag(name = "视频分类管理", description = "视频分类相关操作，包括分类的增删改查、树形结构管理等功能")
public class VideoCategoryController {

    @Autowired
    private VideoCategoryService videoCategoryService;

    /**
     * 获取分类列表（包含视频数量）
     * @return 分类列表数据
     */
    @GetMapping
    @Operation(summary = "获取分类列表", description = "获取所有视频分类列表，包含每个分类下的视频数量统计")
    public Result<List<VideoCategory>> getCategories() {
        return Result.success(videoCategoryService.getAllCategories());
    }

    /**
     * 获取分类树形结构
     * @return 分类树数据
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树形结构", description = "获取视频分类的树形层级结构，用于前端组件展示")
    public Result<List<VideoCategory>> getCategoryTree() {
        return Result.success(videoCategoryService.getCategoryTree());
    }

    /**
     * 获取顶级分类列表
     * @return 顶级分类列表
     */
    @GetMapping("/top")
    @Operation(summary = "获取顶级分类", description = "获取所有启用的顶级分类，用于导航菜单")
    public Result<List<VideoCategory>> getTopCategories() {
        return Result.success(videoCategoryService.getTopCategories());
    }

    /**
     * 根据父级分类ID获取子分类
     * @param parentId 父级分类ID
     * @return 子分类列表
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "获取子分类", description = "根据父级分类ID获取其下的子分类列表")
    public Result<List<VideoCategory>> getChildCategories(
            @Parameter(description = "父级分类ID") @PathVariable Long parentId) {
        return Result.success(videoCategoryService.getChildCategories(parentId));
    }

    /**
     * 根据ID获取分类详情
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情", description = "根据分类ID获取详细信息，包含视频数量和父级分类名称")
    public Result<VideoCategory> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        return Result.success(videoCategoryService.getCategoryById(id));
    }

} 
package com.atguigu.exam.controller.user;

import com.atguigu.exam.common.Result;
import com.atguigu.exam.entity.Paper;
import com.atguigu.exam.service.PaperService;
import com.atguigu.exam.vo.AiPaperVo;
import com.atguigu.exam.vo.PaperVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 试卷控制器 - 处理试卷管理相关的HTTP请求
 * 包括试卷的CRUD操作、AI智能组卷、状态管理等功能
 */
@RestController  // REST控制器，返回JSON数据
@RequestMapping("/api/user/papers")  // 试卷API路径前缀
@Tag(name = "试卷管理", description = "试卷相关操作，包括创建、查询、更新、删除，以及AI智能组卷功能")  // Swagger API分组
@CrossOrigin(origins = "*")
@Slf4j
public class PaperController {

    @Autowired
    private PaperService paperService;


    /**
     * 获取所有试卷列表（支持模糊搜索和状态筛选）
     */
    @GetMapping("/list")  // 处理GET请求
    @Operation(summary = "获取试卷列表", description = "支持按名称模糊搜索和状态筛选的试卷列表查询")  // API描述
    public Result<List<Paper>> listPapers(
            @Parameter(description = "试卷名称，支持模糊查询") @RequestParam(required = false) String name,
            @Parameter(description = "试卷状态，可选值：DRAFT/PUBLISHED/STOPPED") @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Paper> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!ObjectUtils.isEmpty(name), Paper::getName, name);
        queryWrapper.eq(!ObjectUtils.isEmpty(status), Paper::getStatus, status);
        List<Paper> paperList = paperService.list(queryWrapper);
        log.info("试卷列表接口调用成功！本次条件：name = {} , status = {} , 查询列表为：{}",
                name, status, paperList);
        return Result.success(paperList);
    }

    /**
     * 获取试卷详情（包含题目）
     */
    @GetMapping("/{id}")  // 处理GET请求
    @Operation(summary = "获取试卷详情", description = "获取试卷的详细信息，包括试卷基本信息和包含的所有题目")  // API描述
    public Result<Paper> getPaperById(@Parameter(description = "试卷ID") @PathVariable Integer id) {
        Paper paper = paperService.customPaperDetailById(id);
        log.info("获取试卷详情接口调用成功！本次条件：id = {} , 获取的试卷为：{}", id, paper);
        return Result.success(paper);
    }

} 
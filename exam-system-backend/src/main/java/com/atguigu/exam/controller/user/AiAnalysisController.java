package com.atguigu.exam.controller.user;

import com.atguigu.exam.common.Result;
import com.atguigu.exam.service.AiAnalysisService;
import com.atguigu.exam.vo.AiAnalysisResultVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI分析控制器 - 处理学生学习分析相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@Tag(name = "AI学习分析", description = "学生学习分析报告生成功能")
@Validated
public class AiAnalysisController {

    @Autowired
    private AiAnalysisService aiAnalysisService;

    /**
     * 生成学生学习分析报告（简化版）
     *
     * @param studentName 学生姓名
     * @return 分析报告结果
     */
    @PostMapping("/analysis")
    @Operation(summary = "生成学习分析报告", description = "根据学生姓名生成个性化学习分析报告（仅文字报告）")
    public Result<Map<String, String>> generateAnalysis(
            @Parameter(description = "学生姓名", required = true)
            @RequestParam @NotBlank(message = "学生姓名不能为空") String studentName) {

        log.info("收到学习分析请求，学生姓名：{}", studentName);

        String report = aiAnalysisService.generateAnalysisReport(studentName);

        Map<String, String> result = new HashMap<>();
        result.put("report", report);

        return Result.success(result, "分析报告生成成功");
    }

    /**
     * 生成完整的学生学习分析结果（包含图表数据和课程推荐）
     *
     * @param studentName 学生姓名
     * @return 完整分析结果
     */
    @PostMapping("/analysis/full")
    @Operation(summary = "生成完整学习分析", description = "根据学生姓名生成完整的学习分析结果，包含图表数据、课程推荐和文字报告")
    public Result<AiAnalysisResultVo> generateFullAnalysis(
            @Parameter(description = "学生姓名", required = true)
            @RequestParam @NotBlank(message = "学生姓名不能为空") String studentName) {

        log.info("收到完整学习分析请求，学生姓名：{}", studentName);

        AiAnalysisResultVo result = aiAnalysisService.generateFullAnalysis(studentName);

        return Result.success(result, "完整分析结果生成成功");
    }
}

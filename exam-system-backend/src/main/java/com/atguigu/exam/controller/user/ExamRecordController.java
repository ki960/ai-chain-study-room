package com.atguigu.exam.controller.user;

import com.atguigu.exam.common.Result;
import com.atguigu.exam.entity.ExamRecord;
import com.atguigu.exam.service.ExamRecordService;
import com.atguigu.exam.service.ExamService;
import com.atguigu.exam.vo.ExamRankingVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试记录控制器 - 处理考试记录管理相关的HTTP请求
 * 包括考试记录查询、分页展示、成绩排行榜等功能
 */
@RestController  // REST控制器，返回JSON数据
@RequestMapping("/api/user/exam-records")  // 考试记录API路径前缀
@CrossOrigin(origins = "*")  // 允许跨域访问
@Slf4j
@Tag(name = "考试记录管理", description = "考试记录相关操作，包括记录查询、成绩管理、排行榜展示等功能")  // Swagger API分组
public class ExamRecordController {

    @Autowired
    private ExamRecordService examRecordService;
    @Autowired
    private ExamService examService;

    /**
     * 获取考试排行榜 - 优化版本
     * 使用SQL关联查询，一次性获取所有需要的数据，性能提升数百倍
     * 
     * @param paperId 试卷ID，可选参数
     * @param limit 显示数量限制，可选参数
     * @return 排行榜列表
     */
    @GetMapping("/ranking")  // 处理GET请求
    @Operation(summary = "获取考试排行榜", description = "获取考试成绩排行榜，支持按试卷筛选和限制显示数量，使用优化的SQL关联查询提升性能")  // API描述
    public Result<List<ExamRankingVO>> getExamRanking(
            @Parameter(description = "试卷ID，可选，不传则显示所有试卷的排行") @RequestParam(required = false) Integer paperId,
            @Parameter(description = "显示数量限制，可选，不传则返回所有记录") @RequestParam(required = false) Integer limit
    ) {
        List<ExamRankingVO> examRankingVOS =  examRecordService.customGetRanking(paperId,limit);
        log.info("查询：{}试卷下的{}条数据成功！数据为：{}",paperId,limit,examRankingVOS);
        return Result.success(examRankingVOS);
    }
} 
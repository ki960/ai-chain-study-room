package com.atguigu.exam.service;

import com.atguigu.exam.vo.AiAnalysisResultVo;

/**
 * AI分析服务接口
 * 提供学生学习数据分析和AI报告生成功能
 */
public interface AiAnalysisService {

    /**
     * 生成学生学习分析报告（简化版，仅返回文字报告）
     * 
     * @param studentName 学生姓名
     * @return AI生成的学习分析报告（Markdown格式）
     */
    String generateAnalysisReport(String studentName);

    /**
     * 生成完整的学生学习分析结果（包含图表数据和课程推荐）
     * 
     * @param studentName 学生姓名
     * @return 完整的分析结果VO
     */
    AiAnalysisResultVo generateFullAnalysis(String studentName);
}

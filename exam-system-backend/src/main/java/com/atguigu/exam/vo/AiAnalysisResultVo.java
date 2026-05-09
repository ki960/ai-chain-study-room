package com.atguigu.exam.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI分析结果VO - 包含可视化数据和文字报告
 */
@Data
public class AiAnalysisResultVo {
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 考试总次数
     */
    private Integer totalExamCount;
    
    /**
     * 已批阅次数
     */
    private Integer gradedExamCount;
    
    /**
     * 平均得分
     */
    private Double averageScore;
    
    /**
     * 总答题数
     */
    private Integer totalAnswerCount;
    
    /**
     * 正确答题数
     */
    private Integer correctAnswerCount;
    
    /**
     * 错误答题数
     */
    private Integer wrongAnswerCount;
    
    /**
     * 正确率
     */
    private Double correctRate;
    
    /**
     * 图表数据
     */
    private ChartData chartData;
    
    /**
     * 课程推荐列表
     */
    private List<CourseRecommendation> recommendations;
    
    /**
     * AI生成的文字分析报告
     */
    private String analysisReport;
    
    /**
     * 图表数据结构
     */
    @Data
    public static class ChartData {
        
        /**
         * 题型分布数据（饼图）
         */
        private List<PieChartItem> questionTypeDistribution;
        
        /**
         * 知识点错误率（柱状图）
         */
        private List<BarChartItem> categoryErrorRate;
        
        /**
         * 难度级别分布（饼图）
         */
        private List<PieChartItem> difficultyDistribution;
        
        /**
         * 历次考试成绩趋势（折线图）
         */
        private List<LineChartItem> examScoreTrend;
    }
    
    /**
     * 饼图数据项
     */
    @Data
    public static class PieChartItem {
        private String name;
        private Integer value;
        private Double percentage;
    }
    
    /**
     * 柱状图数据项
     */
    @Data
    public static class BarChartItem {
        private String name;
        private Integer totalCount;
        private Integer wrongCount;
        private Double errorRate;
    }
    
    /**
     * 折线图数据项
     */
    @Data
    public static class LineChartItem {
        private String label;
        private Integer score;
        private String date;
    }
    
    /**
     * 课程推荐
     */
    @Data
    public static class CourseRecommendation {
        private Long videoId;
        private String title;
        private String description;
        private String categoryName;
        private String difficulty;
        private String tags;
        private String coverUrl;
        private Integer duration;
        private String durationText;
        private String learningSuggestion;
    }
}

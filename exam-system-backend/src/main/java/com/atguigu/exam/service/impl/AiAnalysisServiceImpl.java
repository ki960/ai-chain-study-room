package com.atguigu.exam.service.impl;

import com.atguigu.exam.entity.*;
import com.atguigu.exam.mapper.*;
import com.atguigu.exam.service.AiAnalysisService;
import com.atguigu.exam.service.DoubaoAiService;
import com.atguigu.exam.vo.AiAnalysisResultVo;
import com.atguigu.exam.vo.AiAnalysisResultVo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI分析服务实现类
 * 实现学生学习数据分析和AI报告生成功能
 */
@Slf4j
@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private AnswerRecordMapper answerRecordMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DoubaoAiService doubaoAiService;

    private static final String SYSTEM_PROMPT = "你是一名专业的教育数据分析专家，擅长根据学生的考试成绩和答题数据进行深度分析和诊断。请基于提供的学生考试记录和答题详情，生成一份全面、专业且具有针对性的学习分析报告。分析过程需严谨科学，建议需具体可行。";

    @Override
    public String generateAnalysisReport(String studentName) {
        log.info("开始生成学生[{}]的学习分析报告", studentName);

        List<ExamRecord> examRecords = findExamRecordsByStudentName(studentName);

        validateExamRecords(examRecords, studentName);

        String studentData = buildStudentDataText(studentName, examRecords);

        String prompt = buildAnalysisPrompt(studentData);

        String analysisReport = callAiService(prompt);

        log.info("学生[{}]的学习分析报告生成完成", studentName);
        return analysisReport;
    }

    @Override
    public AiAnalysisResultVo generateFullAnalysis(String studentName) {
        log.info("开始生成学生[{}]的完整学习分析", studentName);

        AiAnalysisResultVo result = new AiAnalysisResultVo();
        result.setStudentName(studentName);

        List<ExamRecord> examRecords = findExamRecordsByStudentName(studentName);
        validateExamRecords(examRecords, studentName);

        List<AnswerRecord> answerRecords = findAnswerRecordsByExamRecords(examRecords);
        Map<Integer, Question> questionMap = buildQuestionMap(answerRecords);

        fillBasicStatistics(result, examRecords, answerRecords);
        fillChartData(result, examRecords, answerRecords, questionMap);
        fillRecommendations(result, answerRecords, questionMap);
        fillAnalysisReport(result, examRecords, answerRecords, questionMap);

        log.info("学生[{}]的完整学习分析生成完成", studentName);
        return result;
    }

    private List<ExamRecord> findExamRecordsByStudentName(String studentName) {
        QueryWrapper<ExamRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_name", studentName)
                .orderByDesc("start_time");
        return examRecordMapper.selectList(queryWrapper);
    }

    private List<AnswerRecord> findAnswerRecordsByExamRecords(List<ExamRecord> examRecords) {
        List<Integer> examRecordIds = examRecords.stream()
                .map(ExamRecord::getId)
                .map(Long::intValue)
                .collect(Collectors.toList());

        if (examRecordIds.isEmpty()) {
            return Collections.emptyList();
        }

        QueryWrapper<AnswerRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("exam_record_id", examRecordIds);
        return answerRecordMapper.selectList(queryWrapper);
    }

    private Map<Integer, Question> buildQuestionMap(List<AnswerRecord> answerRecords) {
        List<Integer> questionIds = answerRecords.stream()
                .map(AnswerRecord::getQuestionId)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, Question> questionMap = new HashMap<>();
        for (Integer questionId : questionIds) {
            Question question = questionMapper.selectById(questionId);
            if (question != null) {
                questionMap.put(questionId, question);
            }
        }
        return questionMap;
    }

    private void validateExamRecords(List<ExamRecord> examRecords, String studentName) {
        if (examRecords == null || examRecords.isEmpty()) {
            log.error("未找到学生[{}]的任何考试记录", studentName);
            throw new RuntimeException("未找到学生[" + studentName + "]的任何考试记录");
        }
    }

    private void fillBasicStatistics(AiAnalysisResultVo result, List<ExamRecord> examRecords,
            List<AnswerRecord> answerRecords) {
        int gradedCount = 0;
        int totalScore = 0;
        int totalAnswerCount = answerRecords.size();
        int correctCount = 0;

        for (ExamRecord record : examRecords) {
            if ("已批阅".equals(record.getStatus()) && record.getScore() != null) {
                gradedCount++;
                totalScore += record.getScore();
            }
        }

        for (AnswerRecord answer : answerRecords) {
            if (answer.getIsCorrect() != null && answer.getIsCorrect() == 1) {
                correctCount++;
            }
        }

        result.setTotalExamCount(examRecords.size());
        result.setGradedExamCount(gradedCount);
        result.setAverageScore(gradedCount > 0 ? (double) totalScore / gradedCount : 0);
        result.setTotalAnswerCount(totalAnswerCount);
        result.setCorrectAnswerCount(correctCount);
        result.setWrongAnswerCount(totalAnswerCount - correctCount);
        result.setCorrectRate(totalAnswerCount > 0 ? (double) correctCount / totalAnswerCount * 100 : 0);
    }

    private void fillChartData(AiAnalysisResultVo result, List<ExamRecord> examRecords,
            List<AnswerRecord> answerRecords, Map<Integer, Question> questionMap) {
        ChartData chartData = new ChartData();

        chartData.setQuestionTypeDistribution(analyzeQuestionTypeDistribution(answerRecords, questionMap));
        chartData.setCategoryErrorRate(analyzeCategoryErrorRate(answerRecords, questionMap));
        chartData.setDifficultyDistribution(analyzeDifficultyDistribution(answerRecords, questionMap));
        chartData.setExamScoreTrend(analyzeExamScoreTrend(examRecords));

        result.setChartData(chartData);
    }

    private List<PieChartItem> analyzeQuestionTypeDistribution(List<AnswerRecord> answerRecords,
            Map<Integer, Question> questionMap) {
        Map<String, Integer> typeCount = new HashMap<>();
        Map<String, Integer> typeWrongCount = new HashMap<>();

        for (AnswerRecord answer : answerRecords) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question != null) {
                String type = question.getType();
                typeCount.merge(type, 1, Integer::sum);
                if (answer.getIsCorrect() != null && answer.getIsCorrect() != 1) {
                    typeWrongCount.merge(type, 1, Integer::sum);
                }
            }
        }

        List<PieChartItem> items = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            PieChartItem item = new PieChartItem();
            item.setName(getQuestionTypeText(entry.getKey()));
            item.setValue(entry.getValue());
            item.setPercentage(0.0);
            items.add(item);
        }

        return items;
    }

    private List<BarChartItem> analyzeCategoryErrorRate(List<AnswerRecord> answerRecords,
            Map<Integer, Question> questionMap) {
        Map<Long, int[]> categoryStats = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();

        for (AnswerRecord answer : answerRecords) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question != null && question.getCategoryId() != null) {
                Long categoryId = question.getCategoryId();
                categoryStats.computeIfAbsent(categoryId, k -> new int[2]);
                categoryStats.get(categoryId)[0]++;

                if (answer.getIsCorrect() != null && answer.getIsCorrect() != 1) {
                    categoryStats.get(categoryId)[1]++;
                }

                if (!categoryNames.containsKey(categoryId)) {
                    Category category = categoryMapper.selectById(categoryId);
                    if (category != null) {
                        categoryNames.put(categoryId, category.getName());
                    }
                }
            }
        }

        List<BarChartItem> items = new ArrayList<>();
        for (Map.Entry<Long, int[]> entry : categoryStats.entrySet()) {
            BarChartItem item = new BarChartItem();
            item.setName(categoryNames.getOrDefault(entry.getKey(), "未知分类"));
            item.setTotalCount(entry.getValue()[0]);
            item.setWrongCount(entry.getValue()[1]);
            item.setErrorRate(entry.getValue()[0] > 0 ? (double) entry.getValue()[1] / entry.getValue()[0] * 100 : 0);
            items.add(item);
        }

        items.sort((a, b) -> Double.compare(b.getErrorRate(), a.getErrorRate()));
        return items.subList(0, Math.min(10, items.size()));
    }

    private List<PieChartItem> analyzeDifficultyDistribution(List<AnswerRecord> answerRecords,
            Map<Integer, Question> questionMap) {
        Map<String, Integer> difficultyCount = new HashMap<>();

        for (AnswerRecord answer : answerRecords) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question != null && question.getDifficulty() != null) {
                difficultyCount.merge(question.getDifficulty(), 1, Integer::sum);
            }
        }

        List<PieChartItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : difficultyCount.entrySet()) {
            PieChartItem item = new PieChartItem();
            item.setName(getDifficultyText(entry.getKey()));
            item.setValue(entry.getValue());
            item.setPercentage(0.0);
            items.add(item);
        }
        return items;
    }

    private List<LineChartItem> analyzeExamScoreTrend(List<ExamRecord> examRecords) {
        List<LineChartItem> items = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (ExamRecord record : examRecords) {
            LineChartItem item = new LineChartItem();
            item.setLabel("考试" + record.getExamId());
            item.setScore(record.getScore());
            item.setDate(record.getStartTime() != null ? record.getStartTime().format(formatter) : "N/A");
            items.add(item);
        }

        Collections.reverse(items);
        return items;
    }

    private void fillRecommendations(AiAnalysisResultVo result, List<AnswerRecord> answerRecords,
            Map<Integer, Question> questionMap) {
        Set<Long> weakCategoryIds = identifyWeakCategories(answerRecords, questionMap);

        List<CourseRecommendation> recommendations = new ArrayList<>();
        for (Long categoryId : weakCategoryIds) {
            List<Video> videos = findVideosByCategory(categoryId);
            for (Video video : videos) {
                CourseRecommendation rec = new CourseRecommendation();
                rec.setVideoId(video.getId());
                rec.setTitle(video.getTitle());
                rec.setDescription(video.getDescription());
                rec.setCategoryName(video.getCategoryName() != null ? video.getCategoryName() : "");
                rec.setDifficulty("MEDIUM");
                rec.setTags(video.getTags());
                rec.setCoverUrl(video.getCoverUrl());
                rec.setDuration(video.getDuration());
                rec.setDurationText(formatDuration(video.getDuration()));
                rec.setLearningSuggestion(generateLearningSuggestion(video));
                recommendations.add(rec);
            }
        }

        Collections.shuffle(recommendations);
        result.setRecommendations(recommendations.subList(0, Math.min(5, recommendations.size())));
    }

    private Set<Long> identifyWeakCategories(List<AnswerRecord> answerRecords, Map<Integer, Question> questionMap) {
        Map<Long, int[]> categoryStats = new HashMap<>();

        for (AnswerRecord answer : answerRecords) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question != null && question.getCategoryId() != null) {
                Long categoryId = question.getCategoryId();
                categoryStats.computeIfAbsent(categoryId, k -> new int[2]);
                categoryStats.get(categoryId)[0]++;
                if (answer.getIsCorrect() != null && answer.getIsCorrect() != 1) {
                    categoryStats.get(categoryId)[1]++;
                }
            }
        }

        Set<Long> weakCategories = new HashSet<>();
        for (Map.Entry<Long, int[]> entry : categoryStats.entrySet()) {
            double errorRate = (double) entry.getValue()[1] / entry.getValue()[0] * 100;
            if (errorRate > 30) {
                weakCategories.add(entry.getKey());
            }
        }
        return weakCategories;
    }

    private List<Video> findVideosByCategory(Long categoryId) {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", categoryId)
                .eq("status", Video.STATUS_PUBLISHED)
                .orderByDesc("view_count")
                .last("LIMIT 3");
        List<Video> videos = videoMapper.selectList(queryWrapper);

        for (Video video : videos) {
            Category category = categoryMapper.selectById(video.getCategoryId());
            if (category != null) {
                video.setCategoryName(category.getName());
            }
        }
        return videos;
    }

    private String formatDuration(Integer seconds) {
        if (seconds == null)
            return "0:00";
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%d:%02d", minutes, secs);
    }

    private String generateLearningSuggestion(Video video) {
        return String.format("建议学习《%s》课程，该课程涵盖相关知识点，时长约%s，适合巩固薄弱环节。",
                video.getTitle(), formatDuration(video.getDuration()));
    }

    private void fillAnalysisReport(AiAnalysisResultVo result, List<ExamRecord> examRecords,
            List<AnswerRecord> answerRecords, Map<Integer, Question> questionMap) {
        String studentData = buildDetailedStudentData(result, examRecords, answerRecords, questionMap);
        String prompt = buildAnalysisPrompt(studentData);
        result.setAnalysisReport(callAiService(prompt));
    }

    private String buildDetailedStudentData(AiAnalysisResultVo result, List<ExamRecord> examRecords,
            List<AnswerRecord> answerRecords, Map<Integer, Question> questionMap) {
        StringBuilder dataText = new StringBuilder();

        dataText.append("【学生基本信息】\n");
        dataText.append("姓名：").append(result.getStudentName()).append("\n\n");

        dataText.append("【考试统计概览】\n");
        dataText.append("考试总次数：").append(result.getTotalExamCount()).append("次\n");
        dataText.append("已批阅次数：").append(result.getGradedExamCount()).append("次\n");
        dataText.append("平均得分：").append(String.format("%.1f", result.getAverageScore())).append("分\n");
        dataText.append("总答题数：").append(result.getTotalAnswerCount()).append("题\n");
        dataText.append("正确答题数：").append(result.getCorrectAnswerCount()).append("题\n");
        dataText.append("正确率：").append(String.format("%.1f", result.getCorrectRate())).append("%\n\n");

        dataText.append("【知识点错误率分析】\n");
        for (BarChartItem item : result.getChartData().getCategoryErrorRate()) {
            dataText.append("- ").append(item.getName())
                    .append("：总题数").append(item.getTotalCount())
                    .append("，错误数").append(item.getWrongCount())
                    .append("，错误率").append(String.format("%.1f", item.getErrorRate())).append("%\n");
        }

        dataText.append("\n【历次考试成绩】\n");
        for (LineChartItem item : result.getChartData().getExamScoreTrend()) {
            dataText.append("- ").append(item.getDate())
                    .append("：").append(item.getScore() != null ? item.getScore() : "未批阅").append("分\n");
        }

        return dataText.toString();
    }

    private String buildStudentDataText(String studentName, List<ExamRecord> examRecords) {
        StringBuilder dataText = new StringBuilder();

        dataText.append("【学生基本信息】\n");
        dataText.append("姓名：").append(studentName).append("\n\n");

        dataText.append("【考试记录统计】\n");
        dataText.append("考试总次数：").append(examRecords.size()).append("次\n");

        int totalScore = 0;
        int maxPossibleScore = 0;
        int completedCount = 0;
        int highestScore = Integer.MIN_VALUE;
        int lowestScore = Integer.MAX_VALUE;

        for (ExamRecord record : examRecords) {
            if ("已批阅".equals(record.getStatus()) && record.getScore() != null) {
                completedCount++;
                int score = record.getScore();
                totalScore += score;
                maxPossibleScore += 100;
                if (score > highestScore)
                    highestScore = score;
                if (score < lowestScore)
                    lowestScore = score;
            }
        }

        dataText.append("已批阅次数：").append(completedCount).append("次\n");

        if (completedCount > 0) {
            double avgScore = (double) totalScore / completedCount;
            double scoreRate = (double) totalScore / maxPossibleScore * 100;
            double totalDeviation = 0;
            for (ExamRecord record : examRecords) {
                if ("已批阅".equals(record.getStatus()) && record.getScore() != null) {
                    totalDeviation += Math.pow(record.getScore() - avgScore, 2);
                }
            }
            double standardDeviation = Math.sqrt(totalDeviation / completedCount);

            dataText.append("平均得分：").append(String.format("%.1f", avgScore)).append("分\n");
            dataText.append("得分率：").append(String.format("%.1f", scoreRate)).append("%\n");
            dataText.append("最高分：").append(highestScore).append("分\n");
            dataText.append("最低分：").append(lowestScore == Integer.MAX_VALUE ? "N/A" : lowestScore).append("分\n");
            dataText.append("成绩波动：").append(String.format("%.2f", standardDeviation)).append("\n");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dataText.append("\n【详细考试记录】\n");
        for (int i = 0; i < examRecords.size(); i++) {
            ExamRecord record = examRecords.get(i);
            dataText.append((i + 1)).append(". 考试ID：").append(record.getExamId())
                    .append(" | 得分：").append(record.getScore() != null ? record.getScore() : "未批阅")
                    .append(" | 状态：").append(record.getStatus())
                    .append(" | 时间：")
                    .append(record.getStartTime() != null ? record.getStartTime().format(formatter) : "N/A")
                    .append("\n");
        }

        return dataText.toString();
    }

    private String buildAnalysisPrompt(String studentData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_PROMPT).append("\n\n");
        prompt.append("【分析任务】\n");
        prompt.append("请根据以下学生的考试记录和答题数据，进行全面深入的学习分析，并生成专业的分析报告。\n\n");

        prompt.append("【学生考试数据】\n");
        prompt.append(studentData).append("\n\n");

        prompt.append("【分析维度要求】\n");
        prompt.append("请从以下维度进行深度分析：\n");
        prompt.append("1. 整体学习状况评估：基于考试次数、平均分、正确率等核心指标，评估学生的整体学习水平\n");
        prompt.append("2. 成绩趋势分析：分析历次考试成绩的变化趋势，识别进步或退步的迹象\n");
        prompt.append("3. 知识点掌握分析：基于各知识点的错误率数据，识别掌握薄弱的知识点\n");
        prompt.append("4. 学习优势识别：找出学生表现出色的领域，分析成功原因\n");
        prompt.append("5. 薄弱环节诊断：识别错误率高的知识点，分析可能的原因\n");
        prompt.append("\n");

        prompt.append("【报告结构要求】\n");
        prompt.append("报告需包含以下章节，采用Markdown格式输出：\n");
        prompt.append("## 一、整体概览\n");
        prompt.append("  - 考试概况（总次数、已批阅次数）\n");
        prompt.append("  - 核心指标（平均分、正确率）\n");
        prompt.append("## 二、成绩趋势分析\n");
        prompt.append("  - 历次考试成绩变化趋势\n");
        prompt.append("  - 进步/退步原因分析\n");
        prompt.append("## 三、知识点掌握分析\n");
        prompt.append("  - 各知识点正确率对比\n");
        prompt.append("  - 薄弱知识点识别\n");
        prompt.append("## 四、学习优势分析\n");
        prompt.append("  - 优势领域识别\n");
        prompt.append("  - 成功经验总结\n");
        prompt.append("## 五、针对性学习建议\n");
        prompt.append("  - 薄弱知识点提升策略\n");
        prompt.append("  - 学习计划建议\n");
        prompt.append("## 六、总结与鼓励\n");
        prompt.append("  - 总体评价\n");
        prompt.append("  - 鼓励寄语\n");
        prompt.append("\n");

        prompt.append("【写作要求】\n");
        prompt.append("1. 语言风格：专业但不失亲切，适合学生阅读\n");
        prompt.append("2. 数据分析：基于提供的数据进行量化分析，避免主观臆断\n");
        prompt.append("3. 建议可行：提供具体、可操作的学习建议\n");
        prompt.append("4. 结构清晰：使用Markdown标题层次，便于阅读\n");
        prompt.append("5. 篇幅适中：报告总字数控制在800-1200字左右\n");

        return prompt.toString();
    }

    private String callAiService(String prompt) {
        try {
            log.info("开始调用Doubao AI服务生成分析报告");
            String result = doubaoAiService.callDoubaoAi(prompt);
            log.info("Doubao AI服务调用成功");
            return cleanReportResult(result);
        } catch (InterruptedException e) {
            log.error("AI服务调用被中断", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("服务调用被中断，请稍后重试");
        } catch (Exception e) {
            log.error("AI服务调用失败", e);
            throw new RuntimeException("AI服务暂时不可用，请稍后重试");
        }
    }

    private String cleanReportResult(String result) {
        if (result == null) {
            return null;
        }
        String cleanedResult = result.trim();

        cleanedResult = cleanedResult.replaceAll("（全文约\\d+字）\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("\\(全文约\\d+字\\)\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("全文约\\d+字\\s*$", "");

        cleanedResult = cleanedResult.replaceAll("（约\\d+字）\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("\\(约\\d+字\\)\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("约\\d+字\\s*$", "");

        cleanedResult = cleanedResult.replaceAll("（全文\\d+字）\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("\\(全文\\d+字\\)\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("全文\\d+字\\s*$", "");

        cleanedResult = cleanedResult.replaceAll("（\\d+字）\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("\\(\\d+字\\)\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("\\d+字\\s*$", "");

        cleanedResult = cleanedResult.replaceAll("字数：\\d+字?\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("共\\d+字\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("全文共\\d+字\\s*$", "");

        cleanedResult = cleanedResult.replaceAll("（字数\\d+）\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("\\(字数\\d+\\)\\s*$", "");
        cleanedResult = cleanedResult.replaceAll("字数\\d+\\s*$", "");

        return cleanedResult.trim();
    }

    private String getQuestionTypeText(String type) {
        return switch (type) {
            case "CHOICE" -> "选择题";
            case "JUDGE" -> "判断题";
            case "TEXT" -> "简答题";
            default -> "未知题型";
        };
    }

    private String getDifficultyText(String difficulty) {
        return switch (difficulty) {
            case "EASY" -> "简单";
            case "MEDIUM" -> "中等";
            case "HARD" -> "困难";
            default -> "未知";
        };
    }
}

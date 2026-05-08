package com.atguigu.exam.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.exam.entity.*;
import com.atguigu.exam.mapper.AnswerRecordMapper;
import com.atguigu.exam.mapper.ExamRecordMapper;
import com.atguigu.exam.service.AnswerRecordService;
import com.atguigu.exam.service.DoubaoAiService;
import com.atguigu.exam.service.ExamService;
import com.atguigu.exam.service.PaperService;
import com.atguigu.exam.vo.StartExamVo;
import com.atguigu.exam.vo.SubmitAnswerVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 考试服务实现类
 */
@Service
@Slf4j
public class ExamServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamService {

    @Autowired
    private PaperService paperService;
    @Autowired
    AnswerRecordMapper answerRecordMapper;
    @Autowired
    private AnswerRecordService answerRecordService;
    @Autowired
    private DoubaoAiService doubaoAiService;

    @Override
    public ExamRecord saveExam(StartExamVo startExamVo) {
        LambdaQueryWrapper<ExamRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ExamRecord::getStudentName,startExamVo.getStudentName());
        lambdaQueryWrapper.eq(ExamRecord::getExamId,startExamVo.getPaperId());
        lambdaQueryWrapper.eq(ExamRecord::getStatus,"进行中");
        ExamRecord examRecord = getOne(lambdaQueryWrapper);
        if (examRecord != null) {
            log.info("当前用户：{} 正在考试中！",startExamVo.getStudentName());
            return examRecord;
        }

        examRecord = new ExamRecord();
        examRecord.setExamId(startExamVo.getPaperId());
        examRecord.setStudentName(startExamVo.getStudentName());
        examRecord.setStatus("进行中");
        examRecord.setStartTime(LocalDateTime.now());
        examRecord.setWindowSwitches(0);
        save(examRecord);
        return examRecord;
    }

    @Override
    public ExamRecord customGetExamRecordById(Integer id) {
        ExamRecord examRecord = getById(id);
        if (examRecord == null)
            throw new RuntimeException("考试记录不存在！");
        Paper paper = paperService.customPaperDetailById(examRecord.getExamId());
        examRecord.setPaper(paper);

        LambdaQueryWrapper<AnswerRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AnswerRecord::getExamRecordId,id);
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(lambdaQueryWrapper);
        if(!ObjectUtils.isEmpty(answerRecords)){
            List<Long> questionIds = paper.getQuestions().stream().map(Question::getId).collect(Collectors.toList());
            answerRecords.sort((o1, o2) -> questionIds.indexOf(o1.getQuestionId()) - questionIds.indexOf(o2.getQuestionId()));
            examRecord.setAnswerRecords(answerRecords);
        }

        return examRecord;
    }

    @Override
    public void customSubmitAnswer(Integer examRecordId, List<SubmitAnswerVo> answers) throws InterruptedException {
        if (!ObjectUtils.isEmpty(answers)){
            List<AnswerRecord> answerRecords = answers.stream().map(answer -> {
                AnswerRecord answerRecord = new AnswerRecord();
                BeanUtils.copyProperties(answer,answerRecord);
                answerRecord.setExamRecordId(examRecordId);
                return answerRecord;
            }).collect(Collectors.toList());
            answerRecordService.saveBatch(answerRecords);
        }

        ExamRecord examRecord = getById(examRecordId);
        examRecord.setStatus("已批阅");
        examRecord.setEndTime(LocalDateTime.now());
        updateById(examRecord);

        gradeExam(examRecordId);
    }

    @Override
    public ExamRecord gradeExam(Integer examRecordId) throws InterruptedException {
        ExamRecord examRecord = customGetExamRecordById(examRecordId);
        Paper paper = examRecord.getPaper();
        if(paper == null){
            examRecord.setStatus("已批阅");
            examRecord.setAnswers("考试对应的试卷被删除！无法进行成绩判定！");
            examRecord.setScore(0);
            updateById(examRecord);
            log.info("考试对应的试卷被删除！无法进行成绩判定！");
            throw new RuntimeException("考试对应的试卷被删除！无法进行成绩判定！");
        }

        List<AnswerRecord> answerRecords = examRecord.getAnswerRecords();
        if(ObjectUtils.isEmpty(answerRecords)){
            examRecord.setStatus("已批阅");
            examRecord.setScore(0);
            examRecord.setAnswers("没有提交记录！成绩为零！继续加油！");
            updateById(examRecord);
            log.info("没有提交记录！成绩为零！继续加油！");
            return examRecord;
        }

        int correctCount = 0;
        int totalScore = 0;

        Map<Long, Question> questionMap = paper.getQuestions().stream().collect(Collectors.toMap(Question::getId, q -> q));
        for (AnswerRecord answerRecord : answerRecords){
            Question question = questionMap.get(answerRecord.getQuestionId().longValue());
            if(question == null) continue;
            String systemAnswer = question.getAnswer().getAnswer();
            String userAnswer = answerRecord.getUserAnswer();

            if(question.getType().equals("JUDGE"))
                userAnswer = normalizeJudgeAnswer(userAnswer);

            try {
                if(!question.getType().equals("TEXT")){
                    if(systemAnswer.equalsIgnoreCase(userAnswer)){
                        answerRecord.setIsCorrect(1);
                        answerRecord.setScore(question.getPaperScore().intValue());
                    }else{
                        answerRecord.setIsCorrect(0);
                        answerRecord.setScore(0);
                    }
                }
                else{
                    String prompt = doubaoAiService.buildGradingPrompt(question, userAnswer, question.getScore());
                    String result = doubaoAiService.callDoubaoAi(prompt);
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    Integer score = jsonObject.getInteger("score");
                    if(score >= question.getScore()){
                        answerRecord.setIsCorrect(1);
                        answerRecord.setScore(score);
                        answerRecord.setAiCorrection(jsonObject.getString("feedback"));
                    }else if(score <= 0){
                        answerRecord.setIsCorrect(0);
                        answerRecord.setScore(0);
                        answerRecord.setAiCorrection(jsonObject.getString("reason"));
                    }else{
                        answerRecord.setIsCorrect(2);
                        answerRecord.setScore(score);
                        answerRecord.setAiCorrection(jsonObject.getString("reason"));
                    }
                }
            }catch (Exception e){
                answerRecord.setIsCorrect(0);
                answerRecord.setScore(0);
                answerRecord.setAiCorrection("判题过程出错！");
            }
            totalScore += answerRecord.getScore();
            if(answerRecord.getIsCorrect() == 1)
                correctCount++;
        }

        answerRecordService.updateBatchById(answerRecords);

        String prompt = doubaoAiService.buildSummaryPrompt(totalScore, paper.getTotalScore().intValue(), paper.getQuestionCount(), correctCount);
        String summary = doubaoAiService.callDoubaoAi(prompt);

        examRecord.setStatus("已批阅");
        examRecord.setScore(totalScore);
        examRecord.setAnswers(summary);
        updateById(examRecord);
        log.info("考试结束！考试记录ID：{}",examRecordId);
        return examRecord;
    }

    private String normalizeJudgeAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return "";
        }

        String normalized = answer.trim().toUpperCase();
        switch (normalized) {
            case "T":
            case "TRUE":
            case "正确":
                return "TRUE";
            case "F":
            case "FALSE":
            case "错":
                return "FALSE";
            default:
                return normalized;
        }
    }
}
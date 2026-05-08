package com.atguigu.exam.service.impl;


import com.atguigu.exam.common.Result;
import com.atguigu.exam.entity.ExamRecord;
import com.atguigu.exam.entity.Paper;
import com.atguigu.exam.entity.PaperQuestion;
import com.atguigu.exam.entity.Question;
import com.atguigu.exam.mapper.ExamRecordMapper;
import com.atguigu.exam.mapper.PaperMapper;
import com.atguigu.exam.mapper.PaperQuestionMapper;
import com.atguigu.exam.mapper.QuestionMapper;
import com.atguigu.exam.service.PaperQuestionService;
import com.atguigu.exam.service.PaperService;
import com.atguigu.exam.vo.AiPaperVo;
import com.atguigu.exam.vo.PaperVo;
import com.atguigu.exam.vo.RuleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.LifecycleState;
import org.apache.commons.lang3.ObjectUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 试卷服务实现类
 */
@Slf4j
@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    @Autowired
    private PaperMapper paperMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private PaperQuestionService paperQuestionService;
    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Override
    public Paper customPaperDetailById(Integer id) {
        Paper paper = this.getById(id);
        if (paper == null)
            throw new RuntimeException("指定id:%s试卷已经被删除，无法查看详情！".formatted(id));

        List<Question> questionList = questionMapper.customQueryQuestionListByPaperId(id);
        if(ObjectUtils.isEmpty(questionList)){
            paper.setQuestions(new ArrayList<Question>());
            log.warn("试卷中没有题目！可以进行试卷编辑！但是不能用于考试！！,对应试卷id：{}",id);
            return paper;
        }

        questionList.sort((o1, o2) -> Integer.compare(typeToInt(o1.getType()),typeToInt(o2.getType())));
        paper.setQuestions(questionList);

        return paper;
    }

    @Override
    @Transactional
    public Paper customCreatePaper(PaperVo paperVo) {
        Paper paper = new Paper();
        BeanUtils.copyProperties(paperVo,paper);
        paper.setStatus("DRAFT");
        if(ObjectUtils.isEmpty(paperVo.getQuestions())){
            paper.setTotalScore(BigDecimal.ZERO);
            paper.setQuestionCount(0);
            save(paper);
            log.warn("本次{}组卷，没有选择题目！注意没有题目的试卷无法进行考试！！",paper);
            return paper;
        }

        paper.setQuestionCount(paperVo.getQuestions().size());
        paper.setTotalScore(paperVo.getQuestions().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        save(paper);

        List<PaperQuestion> paperQuestionList = paperVo.getQuestions().entrySet().stream()
                .map(entry->new PaperQuestion(paper.getId().intValue(), Long.valueOf(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
        paperQuestionService.saveBatch(paperQuestionList);

        return paper;
    }

    @Transactional
    @Override
    public Paper aiCreatePaper(AiPaperVo aiPaperVo) {
        Paper paper = new Paper();
        BeanUtils.copyProperties(aiPaperVo,paper);
        paper.setStatus("DRAFT");
        save(paper);

        int questionCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;

        for (RuleVo ruleVo : aiPaperVo.getRules()){
            if(ruleVo.getCount() == 0){
                log.warn("在：{}类型下，不需要出题！",ruleVo.getType().name());
                continue;
            }
            LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Question::getType,ruleVo.getType());
            if (ruleVo.getCategoryIds() != null  && !ruleVo.getCategoryIds().isEmpty())
                queryWrapper.in(Question::getCategoryId,ruleVo.getCategoryIds());
            List<Question> questionAllList = questionMapper.selectList(queryWrapper);
            if(questionAllList.isEmpty()){
                log.warn("在{}类型下，题库为空！",ruleVo.getType().name());
                continue;
            }
            int realCount = Math.min(ruleVo.getCount(),questionAllList.size());

            questionCount += realCount;
            totalScore = totalScore.add(BigDecimal.valueOf((long) realCount * ruleVo.getScore()));

            Collections.shuffle(questionAllList);
            List<Question> questionList = questionAllList.subList(0, realCount);
            List<PaperQuestion> paperQuestionList = questionList.stream().map(question ->
                            new PaperQuestion(paper.getId().intValue(), question.getId(), BigDecimal.valueOf(ruleVo.getScore())))
                    .collect(Collectors.toList());
            paperQuestionService.saveBatch(paperQuestionList);
        }
        paper.setQuestionCount(questionCount);
        paper.setTotalScore(totalScore);
        updateById(paper);
        return paper;
    }

    @Override
    public Paper customUpdatePaper(Integer id, PaperVo paperVo) {
        Paper paper = this.getById(id);
        if (paper == null)
            throw new RuntimeException("指定id:%s试卷已经被删除，无法进行修改！".formatted(id));
        if (paper.getStatus().equals("PUBLISHED"))
            throw new RuntimeException("指定id:%s试卷已经发布，无法进行修改！".formatted(id));
        LambdaQueryWrapper<Paper> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Paper::getName,paperVo.getName());
        queryWrapper.ne(Paper::getId,id);
        if (count(queryWrapper) > 0)
            throw new RuntimeException("指定名称的试卷已经存在，请勿重复添加！");

        BeanUtils.copyProperties(paperVo,paper);
        paper.setQuestionCount(paperVo.getQuestions().size());
        paper.setTotalScore(paperVo.getQuestions().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        updateById(paper);

        paperQuestionService.remove(new QueryWrapper<PaperQuestion>().eq("paper_id",id));
        List<PaperQuestion> paperQuestionList = paperVo.getQuestions().entrySet().stream()
                .map(entry->new PaperQuestion(id, Long.valueOf(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
        paperQuestionService.saveBatch(paperQuestionList);

        return paper;
    }

    @Override
    @Transactional
    public void removePaper(Integer id) {
        Paper paper = getById(id);
        if (paper == null || "PUBLISHED".equals(paper.getStatus())){
            throw new RuntimeException("发布状态的试卷不能删除！");
        }
        //2.不能有关联的考试记录
        LambdaQueryWrapper<ExamRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ExamRecord::getExamId,id);
        Long count = examRecordMapper.selectCount(lambdaQueryWrapper);
        if (count > 0){
            throw new RuntimeException("当前试卷：%s 下面有关联 %s条考试记录！无法直接删除！".formatted(id,count));
        }
        //3.删除自身表
        removeById(Long.valueOf(id));
        //4.删除中间表
        paperQuestionService.remove(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId,id));
    }

    private int typeToInt(String type) {
        switch (type) {
            case "CHOICE": return 1; // 选择题
            case "JUDGE": return 2;  // 判断题
            case "TEXT": return 3;   // 简答题
            default: return 4;       // 其他类型
        }
    }
}
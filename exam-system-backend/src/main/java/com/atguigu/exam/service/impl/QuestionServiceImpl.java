package com.atguigu.exam.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.exam.common.CacheConstants;
import com.atguigu.exam.entity.PaperQuestion;
import com.atguigu.exam.entity.Question;
import com.atguigu.exam.entity.QuestionAnswer;
import com.atguigu.exam.entity.QuestionChoice;
import com.atguigu.exam.mapper.PaperQuestionMapper;
import com.atguigu.exam.mapper.QuestionAnswerMapper;
import com.atguigu.exam.mapper.QuestionChoiceMapper;
import com.atguigu.exam.mapper.QuestionMapper;
import com.atguigu.exam.service.DoubaoAiService;
import com.atguigu.exam.service.QuestionService;
import com.atguigu.exam.utils.ExcelUtil;
import com.atguigu.exam.utils.RedisUtils;
import com.atguigu.exam.vo.AiGenerateRequestVo;
import com.atguigu.exam.vo.QuestionImportVo;
import com.atguigu.exam.vo.QuestionQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目Service实现类
 * 实现题目相关的业务逻辑
 */
@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private QuestionChoiceMapper questionChoiceMapper;
    @Autowired
    private QuestionAnswerMapper questionAnswerMapper;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private PaperQuestionMapper paperQuestionMapper;
    @Autowired
    private DoubaoAiService doubaoAiService;

    @Override
    public void queryQuestionListByPage(Page<Question> questionPage, QuestionQueryVo questionQueryVo) {
        questionMapper.selectQuestionListPage(questionPage, questionQueryVo);
    }

    @Override
    public void queryQuestionListByStream(Page<Question> questionPage, QuestionQueryVo questionQueryVo) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(questionQueryVo.getCategoryId()!=null,
                Question::getCategoryId, questionQueryVo.getCategoryId());
        wrapper.eq(!ObjectUtils.isEmpty(questionQueryVo.getDifficulty()),
                Question::getDifficulty,questionQueryVo.getDifficulty());
        wrapper.eq(!ObjectUtils.isEmpty(questionQueryVo.getType()),
                Question::getType, questionQueryVo.getType());
        wrapper.like(!ObjectUtils.isEmpty(questionQueryVo.getKeyword()),
                Question::getTitle, questionQueryVo.getKeyword());
        wrapper.orderByDesc(Question::getCreateTime);

        page(questionPage,wrapper);

        if(ObjectUtils.isEmpty(questionPage.getRecords())){
            log.debug("没有符合题目");
            return;
        }

        fullQuestionChoiceAndAnswer(questionPage.getRecords());
    }

    private void fullQuestionChoiceAndAnswer(List<Question> questionList) {
        List<Long> questionIds = questionList.stream()
                .map(Question::getId).collect(Collectors.toList());
        LambdaQueryWrapper<QuestionChoice> questionChoiceWrapper = new LambdaQueryWrapper<>();
        questionChoiceWrapper.in(QuestionChoice::getQuestionId, questionIds);
        List<QuestionChoice> questionChoices = questionChoiceMapper.selectList(questionChoiceWrapper);

        LambdaQueryWrapper<QuestionAnswer> questionAnswerWrapper = new LambdaQueryWrapper<>();
        questionAnswerWrapper.in(QuestionAnswer::getQuestionId, questionIds);
        List<QuestionAnswer> questionAnswers = questionAnswerMapper.selectList(questionAnswerWrapper);

        Map<Long, QuestionAnswer> answerMap = questionAnswers.stream()
                .collect(Collectors.toMap(QuestionAnswer::getQuestionId, a -> a));
        Map<Long, List<QuestionChoice>> choiceMap = questionChoices.stream()
                .collect(Collectors.groupingBy(QuestionChoice::getQuestionId));

        questionList.forEach(question -> {
            question.setAnswer(answerMap.get(question.getId()));
            if (question.getType().equals("CHOICE")){
                List<QuestionChoice> choices = choiceMap.get(question.getId());
                if(!ObjectUtils.isEmpty( choices)){
                    choices.sort(Comparator.comparing(QuestionChoice::getSort));
                    question.setChoices(choices);
                }
            }
        });
    }

    @Override
    public Question queryQuestionById(Long id) {
        Question question = getById(id);
        if(question == null){
            throw new RuntimeException("没有id为%s的题目".formatted(id));
        }

        LambdaQueryWrapper<QuestionAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionAnswer::getQuestionId, id);
        QuestionAnswer questionAnswer = questionAnswerMapper.selectOne(wrapper);
        question.setAnswer(questionAnswer);

        if(question.getType().equals("CHOICE")){
            LambdaQueryWrapper<QuestionChoice> questionChoiceWrapper = new LambdaQueryWrapper<>();
            questionChoiceWrapper.eq(QuestionChoice::getQuestionId, id);
            List<QuestionChoice> questionChoices = questionChoiceMapper.selectList(questionChoiceWrapper);
            if(!ObjectUtils.isEmpty(questionChoices)){
                questionChoices.sort(Comparator.comparing(QuestionChoice::getSort));
                question.setChoices(questionChoices);
            }else {
                throw new RuntimeException("没有id为%s的题目选项".formatted(id));
            }
        }
        new Thread(()->{
            incrementQuestionScore(id);
        }).start();
        return question;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveQuestion(Question question) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getType, question.getType());
        queryWrapper.eq(Question::getTitle, question.getTitle());
        if (count(queryWrapper) > 0)
            throw new RuntimeException("在%s类型次下题目%s已存在".formatted(question.getType(), question.getTitle()));
        save(question);

        QuestionAnswer answer = question.getAnswer();
        answer.setQuestionId(question.getId());
        if(question.getType().equals("CHOICE")){
            List<QuestionChoice> choices = question.getChoices();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < choices.size(); i++){
                QuestionChoice choice = choices.get(i);
                choice.setSort(i);
                choice.setQuestionId(question.getId());
                questionChoiceMapper.insert(choice);
                if(choice.getIsCorrect()){
                    if(!sb.isEmpty())
                        sb.append(",");
                    sb.append((char)('A'+i));
                }
            }
            answer.setAnswer(sb.toString());
        }
        questionAnswerMapper.insert(answer);
    }

    @Override
    public void updateQuestion(Long id, Question question) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getTitle, question.getTitle());
        queryWrapper.eq(Question::getType, question.getType());
        queryWrapper.ne(Question::getId, question.getId());
        if (count(queryWrapper) > 0)
            throw new RuntimeException("在%s类型次下题目%s已存在".formatted(question.getType(), question.getTitle()));
        updateById(question);

        QuestionAnswer answer = question.getAnswer();
        answer.setQuestionId(question.getId());

        if(question.getType().equals("CHOICE")){
            questionChoiceMapper.delete(new LambdaQueryWrapper<QuestionChoice>().eq(QuestionChoice::getQuestionId, question.getId()));
            List<QuestionChoice> choices = question.getChoices();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < choices.size(); i++){
                QuestionChoice choice = choices.get(i);
                choice.setId(null);
                choice.setCreateTime(null);
                choice.setUpdateTime(null);
                choice.setSort(i);
                choice.setQuestionId(question.getId());
                questionChoiceMapper.insert(choice);
                if(choice.getIsCorrect()){
                    if(!sb.isEmpty())
                        sb.append(",");
                    sb.append((char)('A'+i));
                }
            }
            answer.setAnswer(sb.toString());
        }
        questionAnswerMapper.updateById(answer);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void romoveQuestion(Long id) {
        LambdaQueryWrapper<PaperQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaperQuestion::getQuestionId, id);
        if (paperQuestionMapper.selectCount(queryWrapper) > 0)
            throw new RuntimeException("id为%s的题目已存在于试卷中，请先从试卷中删除");
        removeById(id);
        questionChoiceMapper.delete(new LambdaQueryWrapper<QuestionChoice>().eq(QuestionChoice::getQuestionId, id));
        questionAnswerMapper.delete(new LambdaQueryWrapper<QuestionAnswer>().eq(QuestionAnswer::getQuestionId, id));
        redisUtils.zRemove(CacheConstants.POPULAR_QUESTIONS_KEY, id);
    }

    @Override
    public List<Question> getPopularQuestions(Integer size) {
        List<Question> popularQuestions = new ArrayList<>();
        Set<Object> popularIds = redisUtils.zReverseRange(CacheConstants.POPULAR_QUESTIONS_KEY, 0, size-1);
        if(!ObjectUtils.isEmpty(popularIds)){
             List<Long> longList = popularIds.stream().map(id->Long.valueOf(id.toString())).collect(Collectors.toList());
             for(Long id : longList){
                 Question question = getById(id);
                 if(question != null)
                     popularQuestions.add(question);
             }
        }

        int diff = size - popularQuestions.size();
        if(diff > 0){
            LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(Question::getScore);
            List<Long> exitQestionIds = popularQuestions.stream().map(Question::getId).collect(Collectors.toList());
            queryWrapper.notIn(!exitQestionIds.isEmpty(),Question::getId, exitQestionIds);
            queryWrapper.last("LIMIT " + diff);
            List<Question> questionList = list(queryWrapper);
            popularQuestions.addAll(questionList);
        }

        fullQuestionChoiceAndAnswer(popularQuestions);
        return popularQuestions;
    }

    @Override
    public List<QuestionImportVo> previeExcel(MultipartFile file) throws IOException {
        if (file.isEmpty())
            throw new RuntimeException("生成预览数据表格文件为空");
        String filename = file.getOriginalFilename();
        if(filename.endsWith(".xlsx") && filename.endsWith(".xls"))
            throw new RuntimeException("上传文件格式出错，请上传xls或xlsx文件");
        List<QuestionImportVo> questionImportVoList = ExcelUtil.parseExcel(file);
        return questionImportVoList;
    }

    @Override
    public String importQuestions(List<QuestionImportVo> questions) {
        if (questions.isEmpty())
            return "导入数据为空";
        int successCount = 0;
        int failCount = 0;
        for(QuestionImportVo questionImportVo : questions){
            try{
                Question question = new Question();
                BeanUtils.copyProperties(questionImportVo, question);
                if(question.getType().equals("CHOICE")){
                    List<QuestionChoice> questionchoices = new ArrayList<>(questionImportVo.getChoices().size());
                    for(QuestionImportVo.ChoiceImportDto choice : questionImportVo.getChoices()){
                        QuestionChoice questionChoice = new QuestionChoice();
                        questionChoice.setContent(choice.getContent());
                        questionChoice.setIsCorrect(choice.getIsCorrect());
                        questionChoice.setSort(choice.getSort());
                        questionchoices.add(questionChoice);
                    }
                    question.setChoices(questionchoices);
                }

                QuestionAnswer answer = new QuestionAnswer();
                if (question.getType().equals("JUDGE"))
                    answer.setAnswer(questionImportVo.getAnswer().toUpperCase());
                else
                    answer.setAnswer(questionImportVo.getAnswer());
                answer.setKeywords(questionImportVo.getKeywords());
                question.setAnswer(answer);

                saveQuestion(question);
                successCount++;
            }catch(Exception e){
                log.error("{}问题保存失败",questionImportVo.getTitle());
                failCount++;
            }
        }

        int totalCount = successCount + failCount;
        String result = "一共" + totalCount + "道题，成功" + successCount + "道题，失败" + failCount + "道题";
        return result;
    }

    @Override
    public List<QuestionImportVo> aiGenerateQuestions(AiGenerateRequestVo request) throws InterruptedException {
        String prompt = doubaoAiService.buildPrompt(request);
        log.info("生成题目的提示为：{}", prompt);

        String response = doubaoAiService.callDoubaoAi(prompt);
        log.info("生成题目的结果为：{}", response);

        int startIndex = response.indexOf("```json");
        int endIndex = response.lastIndexOf("```");

        if(startIndex != 1 && endIndex != -1 && startIndex < endIndex){
            String resultJson = response.substring(startIndex + 7, endIndex);

            JSONObject jsonObject = JSONObject.parseObject(resultJson);
            JSONArray questions = jsonObject.getJSONArray("questions");
            List<QuestionImportVo> questionImportVoList = new ArrayList<>();

            for(int i = 0; i < questions.size(); i++){
                JSONObject question = questions.getJSONObject(i);
                QuestionImportVo questionImportVo = new QuestionImportVo();
                questionImportVo.setTitle(question.getString("title"));
                questionImportVo.setType(question.getString("type"));
                questionImportVo.setMulti(question.getBoolean("multi"));
                questionImportVo.setCategoryId(request.getCategoryId());
                questionImportVo.setDifficulty(question.getString("difficulty"));
                questionImportVo.setScore(question.getInteger("score"));
                questionImportVo.setAnalysis(question.getString("analysis"));
                questionImportVo.setAnswer(question.getString("answer"));

                if (question.getString("type").equals("CHOICE")){
                    JSONArray choices = question.getJSONArray("choices");
                    List<QuestionImportVo.ChoiceImportDto> choiceImportDtoList = new ArrayList<>();
                    for(int j = 0; j < choices.size(); j++){
                        QuestionImportVo.ChoiceImportDto choiceImportDto = new QuestionImportVo.ChoiceImportDto();
                        choiceImportDto.setContent(choices.getJSONObject(j).getString("content"));
                        choiceImportDto.setIsCorrect(choices.getJSONObject(j).getBoolean("isCorrect"));
                        choiceImportDto.setSort(choices.getJSONObject(j).getInteger("sort"));
                        choiceImportDtoList.add(choiceImportDto);
                    }
                    questionImportVo.setChoices(choiceImportDtoList);
                }
                questionImportVoList.add(questionImportVo);
            }
            return questionImportVoList;
        }

        throw new RuntimeException("ai生成题目结果结构错误，无法进行解析，具体数据为：{}".formatted(response));
    }

    public void incrementQuestionScore(Long questionId) {
        Double score = redisUtils.zIncrementScore(CacheConstants.POPULAR_QUESTIONS_KEY, questionId, 1);
        log.info("题目id为{}的题目得分已增加1分，当前得分为{}", questionId, score);
    }
}
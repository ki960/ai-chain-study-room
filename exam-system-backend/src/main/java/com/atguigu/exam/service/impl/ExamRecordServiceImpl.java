package com.atguigu.exam.service.impl;

import com.atguigu.exam.entity.AnswerRecord;
import com.atguigu.exam.entity.ExamRecord;
import com.atguigu.exam.entity.Paper;
import com.atguigu.exam.mapper.AnswerRecordMapper;
import com.atguigu.exam.mapper.ExamRecordMapper;
import com.atguigu.exam.service.ExamRecordService;
import com.atguigu.exam.service.PaperService;
import com.atguigu.exam.vo.ExamRankingVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考试记录Service实现类
 * 实现考试记录相关的业务逻辑
 */
@Service
@Slf4j
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {

    @Autowired
    private PaperService paperService;
    @Autowired
    private AnswerRecordMapper answerRecordMapper;
    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Override
    public void pageExamRecords(Page<ExamRecord> examRecordPage, String studentName, String studentNumber, Integer status, String startDate, String endDate) {
        LambdaQueryWrapper<ExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!ObjectUtils.isEmpty(studentName),ExamRecord::getStudentName, studentName);
        if (!ObjectUtils.isEmpty(status)){
            String strStatus = switch (status) {
                case 0 -> "进行中";
                case 1 -> "已完成";
                case 2 -> "已批阅";
                default -> null;
            };
            queryWrapper.eq(ExamRecord::getStatus, strStatus);
        }
        queryWrapper.ge(!ObjectUtils.isEmpty(startDate),ExamRecord::getStartTime, startDate);
        queryWrapper.le(!ObjectUtils.isEmpty(endDate),ExamRecord::getEndTime, endDate);
        page(examRecordPage, queryWrapper);

        List<ExamRecord> examRecordList = examRecordPage.getRecords();
        if(ObjectUtils.isEmpty(examRecordList)){
            log.debug("考试记录列表为空");
            return;
        }

        List<Integer> paperIds = examRecordList.stream().map(ExamRecord::getExamId).collect(Collectors.toList());
        List<Paper> paperList = paperService.listByIds(paperIds);
        Map<Long, Paper> paperMap = paperList.stream().collect(Collectors.toMap(Paper::getId, paper -> paper));
        examRecordPage.getRecords().forEach(examRecord -> {
            Paper paper = paperMap.get(examRecord.getExamId().longValue());
            examRecord.setPaper(paper);
        });
    }

    @Override
    public void removeExamRecordById(Integer id) {
        ExamRecord examRecord = getById(id);
        if (examRecord == null) {
            log.info("考试记录不存在！");
            return;
        }
        if (examRecord.getStatus().equals("进行中"))
            throw new RuntimeException("考试进行中，无法删除！");
        removeById(id);
        answerRecordMapper.delete(new LambdaQueryWrapper<AnswerRecord>().eq(AnswerRecord::getExamRecordId, id));
    }

    @Override
    public List<ExamRankingVO> customGetRanking(Integer paperId, Integer limit) {
        return examRecordMapper.customQueryRanking(paperId,limit);
    }
}
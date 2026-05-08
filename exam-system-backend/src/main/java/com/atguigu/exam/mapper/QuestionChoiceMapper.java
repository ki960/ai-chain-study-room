package com.atguigu.exam.mapper;


import com.atguigu.exam.entity.QuestionChoice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题目选项
 */
public interface QuestionChoiceMapper extends BaseMapper<QuestionChoice> {
    @Select("SELECT * FROM question_choices WHERE question_id = #{questionId} AND is_deleted = 0 ORDER BY sort DESC")
    List<QuestionChoice> selectListByQuestionId(Long questionId);
} 
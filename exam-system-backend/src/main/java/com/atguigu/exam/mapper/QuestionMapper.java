package com.atguigu.exam.mapper;


import com.atguigu.exam.entity.Question;
import com.atguigu.exam.vo.QuestionQueryVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 题目Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础的CRUD操作
 */
public interface QuestionMapper extends BaseMapper<Question> {


    @Select("SELECT category_id, COUNT(*) count FROM questions WHERE is_deleted = 0 GROUP BY category_id")
    List<Map<String, Long>> selectCategoryQuestionCount();

    IPage<Question> selectQuestionListPage(IPage<Question> page,@Param("queryVo") QuestionQueryVo queryVo);

    List<Question> customQueryQuestionListByPaperId(Integer id);
}
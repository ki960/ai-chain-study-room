package com.atguigu.exam.service;

import com.atguigu.exam.entity.Paper;
import com.atguigu.exam.vo.AiPaperVo;
import com.atguigu.exam.vo.PaperVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 试卷服务接口
 */
public interface PaperService extends IService<Paper> {

    Paper customPaperDetailById(Integer id);

    Paper customCreatePaper(PaperVo paperVo);

    Paper aiCreatePaper(AiPaperVo aiPaperVo);

    Paper customUpdatePaper(Integer id, PaperVo paperVo);

    void removePaper(Integer id);
}
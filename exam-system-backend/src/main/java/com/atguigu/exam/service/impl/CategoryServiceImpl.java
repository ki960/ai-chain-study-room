package com.atguigu.exam.service.impl;


import com.atguigu.exam.entity.Category;
import com.atguigu.exam.entity.Question;
import com.atguigu.exam.mapper.CategoryMapper;
import com.atguigu.exam.mapper.QuestionMapper;
import com.atguigu.exam.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<Category> findCategoryList() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        List<Category> categoryList = list(queryWrapper);

        List<Map<String, Long>> mapList = questionMapper.selectCategoryQuestionCount();

        Map<Long, Long> countMap = mapList.stream()
                .collect(Collectors.toMap(m -> m.get("category_id"), m -> m.get("count")));

        for (Category category : categoryList){
            category.setCount(countMap.getOrDefault(category.getId(), 0L));
        }
        return categoryList;
    }

    @Override
    public List<Category> findCategoryTreeList() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        List<Category> categoryList = list(queryWrapper);

        List<Map<String, Long>> mapList = questionMapper.selectCategoryQuestionCount();

        Map<Long, Long> countMap = mapList.stream()
                .collect(Collectors.toMap(m -> m.get("category_id"), m -> m.get("count")));

        for (Category category : categoryList){
            category.setCount(countMap.getOrDefault(category.getId(), 0L));
        }

        Map<Long, List<Category>> longListMap = categoryList.stream()
                                                .collect(Collectors.groupingBy(Category::getParentId));

        List<Category> parentCategoryList = categoryList.stream().filter(c -> c.getParentId() == 0)
                                                .collect(Collectors.toList());

        for (Category parentCategory : parentCategoryList){
            List<Category> children = longListMap.getOrDefault(parentCategory.getId(), new ArrayList<>());
            parentCategory.setChildren(children);
            Long sonCount = children.stream().collect(Collectors.summingLong(Category::getCount));
            parentCategory.setCount(sonCount+parentCategory.getCount());
        }

        return parentCategoryList;
    }

    @Override
    public void addCategory(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, category.getName());
        queryWrapper.eq(Category::getParentId, category.getParentId());
        if (count(queryWrapper) > 0)
            throw new RuntimeException("%s父分类添加失败，该分类%s已存在".formatted(category.getParentId(),category.getName()));
        save(category);
    }

    @Override
    public void updateCategory(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, category.getName());
        queryWrapper.eq(Category::getParentId, category.getParentId());
        queryWrapper.ne(Category::getId, category.getId());
        if (count(queryWrapper) > 0)
            throw new RuntimeException("%s父分类添加失败，该分类%s已存在".formatted(category.getParentId(),category.getName()));
        updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getById(id);
        if (category == null)
            throw new RuntimeException("删除分类失败，该分类不存在");
        if(category.getParentId() == 0)
            throw new RuntimeException("删除分类失败，该分类为根分类");

        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getCategoryId, id);
        Long questionCount = questionMapper.selectCount(queryWrapper);
        if (questionCount > 0)
            throw new RuntimeException("删除分类失败，该分类下有%s个题目".formatted(questionCount));

        removeById(id);
    }
}
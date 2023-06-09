package com.chuhang.manage_course.service;

import com.chuhang.framework.domain.course.ext.CategoryNode;
import com.chuhang.manage_course.dao.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    @Autowired
    CategoryMapper categoryMapper;
    //查询分类
    public CategoryNode findList(){
        return categoryMapper.selectList();
    }
}
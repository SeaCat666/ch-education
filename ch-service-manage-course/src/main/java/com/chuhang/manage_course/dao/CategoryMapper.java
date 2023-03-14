package com.chuhang.manage_course.dao;

import com.chuhang.framework.domain.course.ext.CategoryNode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper {
    //查询分类
    CategoryNode selectList();
}
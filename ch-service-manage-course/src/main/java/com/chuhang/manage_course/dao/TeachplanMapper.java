package com.chuhang.manage_course.dao;

import com.chuhang.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeachplanMapper {
    // 根据课程id查询课程计划，返回课程计划节点
    public TeachplanNode selectList(String courseId);
}
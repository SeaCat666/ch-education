package com.chuhang.manage_course.dao;

import com.chuhang.framework.domain.course.CourseBase;
import com.chuhang.framework.domain.course.ext.CourseInfo;
import com.chuhang.framework.domain.course.ext.TeachplanNode;
import com.chuhang.framework.domain.course.request.CourseListRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface CourseMapper {
   /**
    * 根据id查询课程基础信息
    */
   CourseBase findCourseBaseById(String id);

   //根据课程id查询课程计划，返回课程计划节点
   TeachplanNode selectList(String courseId);


   /**
    * 分页查询课程信息
    */
   List<CourseInfo> findCourseListPage(CourseListRequest courseListRequest);
}

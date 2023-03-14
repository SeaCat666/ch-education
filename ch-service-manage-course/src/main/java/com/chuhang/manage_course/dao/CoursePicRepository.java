package com.chuhang.manage_course.dao;

import com.chuhang.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePicRepository extends JpaRepository<CoursePic,String> {
    /**
     *根据id删除课程图片
     *删除成功返回1否则返回0
     */
    long deleteByCourseid(String courseId);
}

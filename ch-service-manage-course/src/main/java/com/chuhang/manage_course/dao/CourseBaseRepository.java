package com.chuhang.manage_course.dao;

import com.chuhang.framework.domain.course.CourseBase;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA【java持久层api】
 */
public interface CourseBaseRepository extends JpaRepository<CourseBase,String> {
}

package com.chuhang.framework.domain.course.ext;

import com.chuhang.framework.domain.course.CourseBase;
import com.chuhang.framework.domain.course.CourseMarket;
import com.chuhang.framework.domain.course.CoursePic;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@NoArgsConstructor
public class CourseView implements Serializable {
    CourseBase courseBase;//基础信息
    CourseMarket courseMarket;//课程营销
    CoursePic coursePic;//课程图片
    TeachplanNode TeachplanNode;//教学计划
}
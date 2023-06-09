package com.chuhang.framework.domain.portalview;

import com.chuhang.framework.domain.report.ReportCourse;
import com.chuhang.framework.domain.course.CourseBase;
import com.chuhang.framework.domain.course.CourseMarket;
import com.chuhang.framework.domain.course.CoursePic;
import com.chuhang.framework.domain.course.ext.TeachplanNode;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by admin on 2018/2/27.
 */
@Data
@ToString
@Document(collection = "pre_view_course")
public class PreViewCourse implements Serializable{

    @Id
    private String id;
    private CourseBase courseBase;
    private CourseMarket courseMarket;
    private CoursePic coursePic;
    private TeachplanNode teachplan;
    //课程统计信息
    private ReportCourse reportCourse;

}

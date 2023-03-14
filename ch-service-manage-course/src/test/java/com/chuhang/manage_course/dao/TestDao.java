package com.chuhang.manage_course.dao;

import com.chuhang.framework.domain.course.CourseBase;
import com.chuhang.framework.domain.course.CourseMarket;
import com.chuhang.framework.domain.course.ext.CourseInfo;
import com.chuhang.framework.domain.course.request.CourseListRequest;
import com.github.pagehelper.PageHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Test
    /**
     * 测试使用jpa查询
     */
    public void testCourseBaseRepository(){
        Optional<CourseBase> optional = courseBaseRepository.findById("4028e581617f945f01617f9dabc40000");
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }
    }

    @Test
    /**
     * 使用mybatis查询
     */
    public void testCourseMapper(){
        CourseBase courseBase = courseMapper.findCourseBaseById("4028e581617f945f01617f9dabc40000");
        System.out.println(courseBase);
    }

    /*
    *测试分页
    */
    @Test
    public void testPageHelper(){
        PageHelper.startPage(2,1);
        CourseListRequest courseListRequest=new CourseListRequest();
        List<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);

        System.out.println(courseListPage);
    }

    /**
     *测试课程营销
     */
    @Test
    public void testCourseMarket(){
        Optional<CourseMarket> optional = courseMarketRepository.findById("4028e581617f945f01617f9dabc40000");
        if (optional.isPresent()){
            System.out.println("aaaa"+optional.get());
        }
    }
}

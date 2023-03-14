package com.chuhang.api.course;

import com.chuhang.framework.domain.course.CourseBase;
import com.chuhang.framework.domain.course.CourseMarket;
import com.chuhang.framework.domain.course.CoursePic;
import com.chuhang.framework.domain.course.Teachplan;
import com.chuhang.framework.domain.course.ext.CourseInfo;
import com.chuhang.framework.domain.course.ext.CourseView;
import com.chuhang.framework.domain.course.ext.TeachplanNode;
import com.chuhang.framework.domain.course.request.CourseListRequest;
import com.chuhang.framework.domain.course.response.AddCourseResult;
import com.chuhang.framework.domain.course.response.CoursePublishResult;
import com.chuhang.framework.model.response.QueryResponseResult;
import com.chuhang.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "课程管理接口",tags = "课程管理接口，提供课程的增、删、改、查")
public interface CourseControllerApi {
    //根据课程id查询课程计划
    @ApiOperation("课程计划查询")
    TeachplanNode findTeachplanList(String courseId);

    //添加课程计划
    @ApiOperation("添加课程计划")
    ResponseResult addTeachplan(Teachplan teachplan);

    //查询课程列表
    @ApiOperation("查询我的课程列表")
    QueryResponseResult<CourseInfo> findCourseList(int page, int size, CourseListRequest courseListRequest);

    //新增课程
    @ApiOperation("添加课程基础信息")
    AddCourseResult addCourseBase(CourseBase courseBase);

    //查询课程基本信息
    @ApiOperation("查询课程基本信息回显")
    CourseBase  getCoursebaseByCourseId(String courseId);

    //修改课程基本信息
    @ApiOperation("根据id修改课程基本信息")
    ResponseResult updateCourseBase(String courseId,CourseBase courseBase);

    //获取课程营销信息
    @ApiOperation("根据id获取课程营销信息")
    CourseMarket getCourseMarketById(String courseId);

    //修改课程营销计划
    @ApiOperation("根据id修改课程营销信息")
    ResponseResult updateCourseMarket(String courseId,CourseMarket courseMarket);

    //添加课程图片
    @ApiOperation("添加课程图片")
    public ResponseResult addCoursePic(String courseId,String pic);

    //课程图片回显
    @ApiOperation("获取课程图片基础信息")
    public CoursePic findCoursePic(String courseId);

    //删除课程图片
    @ApiOperation("删除课程图片")
    public ResponseResult deleteCoursePic(String courseId);

    //课程详情视图数据查询
    @ApiOperation("课程详情视图数据查询")
    public CourseView courseview(String id);

    //预览课程
    @ApiOperation("课程预览")
    public CoursePublishResult preview(String id);

    //发布课程
    @ApiOperation("课程发布")
    CoursePublishResult publish(String id);
}

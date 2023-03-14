package com.chuhang.manage_course.controller;

import com.chuhang.api.course.CourseControllerApi;
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
import com.chuhang.framework.exception.ExceptionCast;
import com.chuhang.framework.model.response.CommonCode;
import com.chuhang.framework.model.response.QueryResponseResult;
import com.chuhang.framework.model.response.ResponseResult;
import com.chuhang.framework.utils.ChOauth2Util;
import com.chuhang.framework.web.BaseController;
import com.chuhang.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseController extends BaseController implements CourseControllerApi {
    @Autowired
    private CourseService courseService;

    //根据课程id查询课程计划
    @PreAuthorize("hasAuthority('course_teachplan_list')")
    @Override
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId) {
        return courseService.findTeachplanList(courseId);
    }

    //添加课程计划
    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachplan(teachplan);
    }

    //查询课程列表
    @PreAuthorize("hasAuthority('course_find_list')")
    @Override
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult<CourseInfo> findCourseList(
                                                @PathVariable("page") int page,
                                                @PathVariable("size") int size,
                                                CourseListRequest courseListRequest) {

      /*  //当前用户所属单位的id
        String companyId="1";
        //将公司id设置到请求参数对象
        courseListRequest.setCompanyId(companyId);
        //根据公司id查询出用户所属公司/机构的课
        return courseService.findCourseList(page,size,courseListRequest);*/

        //调用工具类取出用户信息
        ChOauth2Util chOauth2Util=new ChOauth2Util();
        ChOauth2Util.UserJwt userJwt = chOauth2Util.getUserJwtFromHeader(request);
        if (null==userJwt){
            ExceptionCast.cast(CommonCode.UNAUTHENTICATED);
        }
        String companyId = userJwt.getCompanyId();
        //将公司id设置到请求参数对象
        courseListRequest.setCompanyId(companyId);
        //根据公司id查询出用户所属公司/机构的课
        return courseService.findCourseList(page,size,courseListRequest);
    }

    //新增课程
    @Override
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    //查询课程基本信息
    @PreAuthorize("hasAuthority('course_get_baseinfo')")
    @Override
    @GetMapping("/coursebase/get/{courseId}")
    public CourseBase getCoursebaseByCourseId(@PathVariable("courseId") String courseId) {
        return courseService.findCourseBaseById(courseId);
    }

    //修改课程基本信息
    @Override
    @PutMapping("/coursebase/update/{courseId}")
    public ResponseResult updateCourseBase(@PathVariable("courseId") String courseId,
                                           @RequestBody CourseBase courseBase) {
        return courseService.updateCourseBaseById(courseId,courseBase);
    }

    //获取课程营销信息
    @Override
    @GetMapping("/coursemarket/get/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

    //修改课程营销计划
    @Override
    @PutMapping("/coursemarket/update/{courseId}")
    public ResponseResult updateCourseMarket(@PathVariable("courseId") String courseId,
                                             @RequestBody CourseMarket courseMarket) {
        return courseService.updateCourseMarketById(courseId,courseMarket);
    }

    //添加课程图片
    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId,
                                       @RequestParam("pic") String pic) {
        return courseService.saveCoursePic(courseId,pic);
    }

    //根据课程id查询对应的课程图片
    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
        return courseService.findCoursepic(courseId);
    }

    //根据课程id删除课程图片
    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }

    //课程详情视图数据查询
    @Override
    @GetMapping("/courseview/{id}")
    public CourseView courseview(@PathVariable("id") String id) {
        return courseService.getCoruseView(id);
    }

    //预览课程
    @Override
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);
    }

    //发布课程
    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable("id") String id) {
        return courseService.publish(id);
    }
}

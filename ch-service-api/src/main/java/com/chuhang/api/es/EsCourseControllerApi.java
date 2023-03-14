package com.chuhang.api.es;

import com.chuhang.framework.domain.course.CoursePub;
import com.chuhang.framework.domain.search.CourseSearchParam;
import com.chuhang.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "课程搜索",tags = "课程搜索")
public interface EsCourseControllerApi {
    @ApiOperation("课程搜索")
    QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam);
}

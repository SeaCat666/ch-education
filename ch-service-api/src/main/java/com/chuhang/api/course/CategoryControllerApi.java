package com.chuhang.api.course;

import com.chuhang.framework.domain.course.ext.CategoryNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

@Api(tags = "课程分类管理")
public interface CategoryControllerApi {

    @ApiOperation("查询分类")
    CategoryNode findList();
}

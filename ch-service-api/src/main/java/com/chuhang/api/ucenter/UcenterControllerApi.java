package com.chuhang.api.ucenter;

import com.chuhang.framework.domain.ucenter.ext.ChUserExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "用户中心",tags = "用户中心管理")
public interface UcenterControllerApi {
    @ApiOperation("获取用户信息")
    ChUserExt getUserext(String username);
}

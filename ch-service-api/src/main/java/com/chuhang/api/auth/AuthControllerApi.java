package com.chuhang.api.auth;

import com.chuhang.framework.domain.ucenter.request.LoginRequest;
import com.chuhang.framework.domain.ucenter.response.JwtResult;
import com.chuhang.framework.domain.ucenter.response.LoginResult;
import com.chuhang.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "用户认证",tags = "用户认证接口")
public interface AuthControllerApi {
    @ApiOperation("登录")
    LoginResult login(LoginRequest loginRequest);

    @ApiOperation("退出")
    ResponseResult logout();

    @ApiOperation("查询userjwt令牌")
    JwtResult userjwt();
}

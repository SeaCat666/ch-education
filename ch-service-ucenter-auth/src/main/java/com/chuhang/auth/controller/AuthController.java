package com.chuhang.auth.controller;

import com.chuhang.api.auth.AuthControllerApi;
import com.chuhang.auth.service.AuthService;
import com.chuhang.framework.domain.ucenter.ext.AuthToken;
import com.chuhang.framework.domain.ucenter.request.LoginRequest;
import com.chuhang.framework.domain.ucenter.response.AuthCode;
import com.chuhang.framework.domain.ucenter.response.JwtResult;
import com.chuhang.framework.domain.ucenter.response.LoginResult;
import com.chuhang.framework.exception.ExceptionCast;
import com.chuhang.framework.model.response.CommonCode;
import com.chuhang.framework.model.response.ResponseResult;
import com.chuhang.framework.utils.CookieUtil;
import com.chuhang.framework.web.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import java.util.Map;

@RestController
public class AuthController extends BaseController implements AuthControllerApi {

    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;
    @Value("${auth.tokenValiditySeconds}")
    long tokenValiditySeconds;

    @Autowired
    AuthService authService;

    /**
    * 用户登录
     * 1：申请令牌
     * 2：将令牌存储redis
    */
    @Override
    @PostMapping("/userlogin")
    //没有写@RequestBody说明LoginRequest不是通过json来传递接收的，而是通过"/userlogin?xxx"来接收
    public LoginResult login(LoginRequest loginRequest) {
        //【0】校验
        //校验账号是否输入
        if (loginRequest==null || StringUtils.isEmpty(loginRequest.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        if (StringUtils.isEmpty(loginRequest.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        //【1】申请令牌
        AuthToken authToken = authService.login(loginRequest.getUsername(), loginRequest.getPassword() , clientId, clientSecret);

        //【2】将短令牌写入cookie
        //2.1访问token=================
        String jti_token = authToken.getJti_token();

        //2.2将访问令牌存储到cookie
        this.saveCookie(jti_token);
        return new LoginResult(CommonCode.SUCCESS,jti_token);
    }

    private void saveCookie(String token){
        Cookie cookie=new Cookie("uid",token);
        //设置可以访问此cookie的域名
        cookie.setDomain(cookieDomain);
        //设置cooki超时时间
        cookie.setMaxAge(cookieMaxAge);
        //设置可以访问此cookie的页面路径
        cookie.setPath("/");
        //设置false表示允许浏览器获取
        cookie.setHttpOnly(false);

        response.addCookie(cookie);
    }

    /**
     * 从redis查询令牌
     * 1、获取前端请求携带的cookie短令牌
     * 2、redis里拿jwt
     * 3、返回jwt对象
     */
    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt() {
        //1.获取cookie中的令牌
       String jti_token=getTokenFormCookie();
       if (StringUtils.isEmpty(jti_token)){
           return new JwtResult(CommonCode.FAIL,null);
       }
       //2.根据短令牌从redis查询jwt
       AuthToken userToken = authService.getUserToken(jti_token);
        if (userToken==null){
            return new JwtResult(CommonCode.FAIL,null);
        }
        //3.返回
        return new JwtResult(CommonCode.SUCCESS, userToken.getAccess_token());
    }

    /**
    *从cookie中访问读取令牌
    */
    private String getTokenFormCookie() {
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        String jti_token = map.get("uid");
        return jti_token;
    }


    /**
     * 退出登录
     */
    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        //1.取出身份令牌
        String token = getTokenFormCookie();
        //2.删除redis中的token
        authService.delToken(token);
        //3.清楚cookie
        clearCookie(token);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     *清除cookie，设置生命周期为0秒即可
     */
    private void clearCookie(String token) {
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token,0,false);
    }

}

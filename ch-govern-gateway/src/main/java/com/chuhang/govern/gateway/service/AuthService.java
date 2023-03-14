package com.chuhang.govern.gateway.service;

import com.chuhang.framework.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    StringRedisTemplate stringredisTemplate;

    //查询身份令牌
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String jti_token = cookieMap.get("uid");
        if(StringUtils.isEmpty(jti_token)){
            return null;
        }
        return jti_token;
    }
    //从header中查询jwt令牌
    public String getJwtFromHeader(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            //拒绝访问
            return null;
        }
        if(!authorization.startsWith("Bearer ")){
            //拒绝访问
            return null;
        }
        return authorization;
    }
    //查询令牌的有效期
    public long getExpire(String jti_token) {
        //token在redis中的key
        String key = "user_token:"+jti_token;
        Long expire = stringredisTemplate.getExpire(key);
        return expire;
    }
}
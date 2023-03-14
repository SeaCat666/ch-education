package com.chuhang.auth.service;

import com.alibaba.fastjson.JSON;
import com.chuhang.framework.client.ChServiceList;
import com.chuhang.framework.domain.ucenter.ext.AuthToken;
import com.chuhang.framework.domain.ucenter.response.AuthCode;
import com.chuhang.framework.domain.ucenter.response.JwtResult;
import com.chuhang.framework.exception.ExceptionCast;
import com.chuhang.framework.model.response.CommonCode;
import com.chuhang.framework.utils.CookieUtil;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    @Value("${auth.tokenValiditySeconds}")
    long tokenValiditySeconds;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     *用户登录认证处理
     * 1.根据密码模式生成令牌
     * 2.存储令牌：整个令牌保存到redis（短令牌保存到cookie交由web层处理）
     */
    public AuthToken login(String username,String password,String clientId,String clientSecret){
        //【1】申请令牌
        AuthToken authToken = applyToken(username,password,clientId, clientSecret);
        if (authToken==null){
            //申请令牌失败
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        //【2】存储令牌：将令牌存储到redis
        String jti_token = authToken.getJti_token();

        String content = JSON.toJSONString(authToken);

        boolean saveTokentoRedis = saveTokentoRedis(jti_token, content, tokenValiditySeconds);

        if(!saveTokentoRedis){
            //存储令牌失败异常
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;
    }

    /**
     *申请令牌
     */
    private AuthToken applyToken(String username,String password,String clientId,String clientSecret){
        //【1】采用客户端负载均衡，从eureka获取认证服务的ip 和端口
        ServiceInstance choose = loadBalancerClient.choose(ChServiceList.CH_SERVICE_UCENTER_AUTH);
        URI uri = choose.getUri();
        String authUrl=uri+"/auth/oauth/token";

        //【4】请求的内容分两部分
        //4.1、构建header信息：包括了http basic认证信息
        MultiValueMap<String,String> headers=new LinkedMultiValueMap<>();
        String httpbasic = httpbasic(clientId, clientSecret);
        headers.add("Authorization",httpbasic);

        //4.2、构建body信息：包括：grant_type、username、passowrd
        MultiValueMap<String,String> body=new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        //【3】构造请求对象
        HttpEntity<MultiValueMap<String,String>> httpEntity=
                new HttpEntity<MultiValueMap<String,String>>(body,headers);
        //指定restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        //【2】远程调用申请令牌，注意发送请求不用restTemplate.getForEntity(url,responseType),局限性太强
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl,//请求的url
                HttpMethod.POST,         //请求的方式
                httpEntity,    //整个http的请求内容，包含请求头和请求体
                Map.class);         //指定返回的类型

        //【5】申请令牌信息
        Map bodyMap = exchange.getBody();

        //【6】判断令牌：如果整个令牌是null或者令牌的三个部分有一个是null，
        if(bodyMap == null ||
                bodyMap.get("access_token") == null ||
                bodyMap.get("refresh_token") == null ||
                bodyMap.get("jti") == null) {//jti是jwt令牌的唯一标识作为用户身份令牌
            //解析springsecurity返回的错误信息
            if (bodyMap != null && bodyMap.get("error_description") != null) {
                String error_description = (String) bodyMap.get("error_description");
                if (error_description.indexOf("UserDetailsService returned null") >= 0) {
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                } else if (error_description.indexOf("坏的凭证") >= 0) {
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);//账号或者密码错误
                }
            }
            return null;
        }

        //【7】将结果添加到token模型对象
        AuthToken authToken=new AuthToken();
        //访问令牌(access_token)
        String access_token = (String) bodyMap.get("access_token");
        //刷新令牌(refresh_token)
        String refresh_token = (String) bodyMap.get("refresh_token");
        //jti，作为用户的身份标识================================================
        String jti_token = (String) bodyMap.get("jti");

        authToken.setAccess_token(access_token);
        authToken.setRefresh_token(refresh_token);
        authToken.setJti_token(jti_token);

        return authToken;
    }

    /**
     *获取 httpbasic 认证串
     */
    private String httpbasic(String clientId,String clientSecret){
        //将客户端id和客户密码拼接，按“客户端id:客户端密码”
        String string=clientId+":"+clientSecret;
        //进行base64编码
        byte[] encode = Base64.encode(string.getBytes());
        return "Basic "+new String(encode);
    }

    /**
     *存储令牌到redis
     * @param jti_token：短令牌
     * @param content：AuthToken的内容
     * @param ttl：过期时间
     */
    private boolean saveTokentoRedis(String jti_token,String content,long ttl){
        //令牌名称
        String name="user_token:"+jti_token;
        //==========

        //保存令牌到redis
        stringRedisTemplate.boundValueOps(name).set(content,ttl, TimeUnit.SECONDS);

        //获取过期时间，返回-2表示失效
        Long expire = stringRedisTemplate.getExpire(name);
        return expire>0;
    }

    /**
     * 从redis查询令牌
     */
    public AuthToken getUserToken(String token){
       String userToken="user_token:"+token;
        String userTokenString = stringRedisTemplate.boundValueOps(userToken).get();
        //System.out.println(userTokenString+"============");
        if (StringUtils.isEmpty(userTokenString)){
            return null;
        }
        AuthToken authToken = JSON.parseObject(userTokenString, AuthToken.class);
        return authToken;
    }

    /**
     * 从redis中删除令牌
     */
    public boolean delToken(String token){
        String key="user_token:"+token;
        Boolean delete = stringRedisTemplate.delete(key);
        return delete;
    }
}

package com.chuhang.auth;

import com.chuhang.framework.client.ChServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import com.sun.jersey.core.util.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestClient {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient client;

    @Test
    public void testClient(){
        //【1】采用客户端负载均衡，从注册中心获取本服务的IP地址和端口
        ServiceInstance serviceInstance = client.choose(ChServiceList.CH_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri();
        //拼凑符合Oauth2给我们提供的通过密码方式获取令牌的url
        String authUrl=uri+"/auth/oauth/token";
        //【4】请求的内容部分
        //URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType
        // url就是 申请令牌的url /oauth/token
        //method http的方法类型
        //requestEntity请求内容
        //responseType，将响应的结果生成的类型
        //请求的内容分两部分
        //4.1 构建header头部信息，（http basic认证信息）
        MultiValueMap<String,String> headers=new LinkedMultiValueMap<>();
        String httpbasic = httpbasic("ChWebApp", "123");

        //"Basic WGNXZWJBcHA6WGNXZWJBcHA="
        headers.add("Authorization",httpbasic);

        //4.2 构建body(包括grant_type、username、password)
        MultiValueMap<String,String> body=new LinkedMultiValueMap<>();
        body.add("username","lq");
        body.add("password","123");
        body.add("grant_type","password");

        //【3】构建请求对象
        HttpEntity<MultiValueMap<String,String>> httpEntity=new HttpEntity<>(body,headers);
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

        //【2】远程调用，通过密码模式申请令牌
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl,//请求的url
                HttpMethod.POST,//请求的方式
                httpEntity,//整个http请求的内容
                Map.class);
        //获取申请成功的令牌
        Map bodyMap = exchange.getBody();
        System.out.println(bodyMap);
    }

    private String httpbasic(String clientId,String clientSecret){
        //将客户端id和客户密码拼接，按“客户端id:客户端密码”
        String string=clientId+":"+clientSecret;
        //进行base64编码
        byte[] encode = Base64.encode(string.getBytes());
        return "Basic "+new String(encode);
    }
}

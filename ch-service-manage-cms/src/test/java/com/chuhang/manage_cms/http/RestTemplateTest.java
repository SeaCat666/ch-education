package com.chuhang.manage_cms.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/*
*RestTemplate测试类
*/
@SpringBootTest
@RunWith(SpringRunner.class)
public class RestTemplateTest {

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testRestTemplate(){
        //参数1：请求的url，参数2：返回的类型（咱们这需要map）
        ResponseEntity<Map> template = restTemplate.getForEntity
                ("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f",Map.class);
        //获取map
        Map map = template.getBody();

        System.out.println(map);

    }
}

package com.chuhang.manage_course;

import com.chuhang.framework.interceptor.FeignClientInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author lq
 * @version 1.0
 **/
@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication
@EntityScan("com.chuhang.framework.domain.course")//扫描实体类
@ComponentScan(basePackages={"com.chuhang.api"})//扫描接口
@ComponentScan(basePackages={"com.chuhang.manage_course"})
@ComponentScan(basePackages={"com.chuhang.framework"})//扫描common下的所有类
public class ManageCourseApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ManageCourseApplication.class, args);
    }
    @Bean
    public FeignClientInterceptor feignClientInterceptor(){
        return new FeignClientInterceptor();
    }
}

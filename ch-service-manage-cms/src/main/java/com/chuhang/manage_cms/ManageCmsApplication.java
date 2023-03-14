package com.chuhang.manage_cms;


import com.chuhang.manage_cms.config.MongoConfig;
import com.chuhang.manage_cms.config.HttpConfig;
import com.chuhang.manage_cms.config.RabbitmqConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
@EnableFeignClients
@EnableEurekaClient //标识这是一个client服务
@SpringBootApplication//一旦运行main方法，会扫描这个启动类这个包以及子包下面所有Bean
@EntityScan("com.chuhang.framework.domain.cms")// 扫描实体类
@ComponentScan(basePackages = {"com.chuhang.api"})// 扫描接口
@ComponentScan(basePackages = {"com.chuhang.manage_cms"})// 扫描本项目下的所有类，
// 可以不写，写了维护人员马上就知道扫描了哪些类
@ComponentScan(basePackages="com.chuhang.framework")// 扫描common工程下的类
@Import({HttpConfig.class, MongoConfig.class, RabbitmqConfig.class})
public class ManageCmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManageCmsApplication.class,args);
    }
}

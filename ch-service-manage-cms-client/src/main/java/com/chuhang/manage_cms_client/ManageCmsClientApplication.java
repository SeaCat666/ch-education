package com.chuhang.manage_cms_client;

import com.chuhang.manage_cms_client.config.MongoConfig;
import com.chuhang.manage_cms_client.config.RabbitmqConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EntityScan("com.chuhang.framework.domain.cms")//扫描实体类
@ComponentScan(basePackages={"com.chuhang.framework"})//扫描common包下的类
@ComponentScan(basePackages={"com.chuhang.manage_cms_client"})//扫描本项目下的所有类
@Import({RabbitmqConfig.class, MongoConfig.class})
public class ManageCmsClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManageCmsClientApplication.class,args);
    }
}
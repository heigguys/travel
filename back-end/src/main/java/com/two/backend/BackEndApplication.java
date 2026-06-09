package com.two.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.two.backend.mapper")
@EnableScheduling
/**
 * 后端应用入口，启动 Spring Boot 并扫描 MyBatis Mapper 接口。
 */
public class BackEndApplication {

    /**
     * 启动公司旅行管理系统后端服务。
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BackEndApplication.class, args);
    }

}

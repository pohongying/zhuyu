package com.luoyi.implatform;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;



@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan(basePackages = {"com.luoyi.implatform.mapper"})
@SpringBootApplication
public class IMPlatformApp {

    public static void main(String[] args) {
        SpringApplication.run(IMPlatformApp.class, args);
    }

}

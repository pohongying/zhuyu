package com.luoyi.imclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@ComponentScan(basePackages = {"com.luoyi.imclient", "com.luoyi.imcommon"}) // 开启包扫描
public class IMAutoConfiguration {

}

package com.xzj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "com.xzj.message",
        "com.xzj.live", "com.xzj.other", "com.xzj.common", "com.xzj.config",
        "cn.lili.common", "cn.lili.cache", "cn.lili.message", "cn.lili.mybatis",
        "cn.lili.rocketmq", "cn.lili.trigger"
})
@EnableFeignClients(basePackages = {"com.xzj.member", "com.xzj.config", "com.xzj.order", "com.xzj.message"})
public class MessageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageServiceApplication.class, args);
    }

}

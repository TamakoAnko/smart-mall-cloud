package com.xzj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {
        "com.xzj.member.controller", "com.xzj.member.entity", "com.xzj.member.mapper",
        "com.xzj.member.service", "com.xzj.member.serviceimpl", "com.xzj.member.aop",
        "com.xzj.member.token",
        "com.xzj.member.sms",
        "com.xzj.member.verification",
        "com.xzj.passport.controller", "com.xzj.passport.connect",
        "com.xzj.config",
        "cn.lili.common", "cn.lili.cache", "cn.lili.message", "cn.lili.mybatis",
        "cn.lili.rocketmq", "cn.lili.trigger"
})
@MapperScan({
        "com.xzj.member.verification.mapper",
        "com.xzj.member.sms.mapper"
})
@EnableFeignClients(basePackages = {"com.xzj.config", "com.xzj.order", "com.xzj.message"})
public class MemberServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemberServiceApplication.class, args);
    }

}

package com.xzj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 管理端和商家端 API 服务入口。
 * 控制器复用 manager-api，业务源码逐步迁移到本服务。
 */
@SpringBootApplication(scanBasePackages = {
        // 本服务自有代码（含 com.xzj.config 下的 Mapper 配置）
        "com.xzj",
        "cn.lili.controller", "cn.lili.event", "cn.lili.init", "cn.lili.listener",
        "cn.lili.message", "cn.lili.modules", "cn.lili.security", "cn.lili.timetask",
        "cn.lili.trigger",
        // lili-common 共享库中需要注册为 Bean 的包，与买家端各服务的扫描列表保持一致
        "cn.lili.cache", "cn.lili.common", "cn.lili.config", "cn.lili.mybatis",
        "cn.lili.rocketmq"
})
@EnableAsync
@EnableFeignClients(basePackages = {"com.xzj.member", "com.xzj.config", "com.xzj.order"})
public class ManagerServiceApplication {

    public static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(ManagerServiceApplication.class, args);
    }
}

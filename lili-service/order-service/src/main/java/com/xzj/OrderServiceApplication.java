package com.xzj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {
        "com.xzj.order",
        "com.xzj.payment", "com.xzj.wallet", "com.xzj.config",
        "cn.lili.common", "cn.lili.cache", "cn.lili.message", "cn.lili.mybatis",
        "cn.lili.rocketmq", "cn.lili.trigger"
})
@MapperScan({
        "com.xzj.order.aftersale.mapper",
        "com.xzj.order.mapper",
        "com.xzj.order.trade.mapper", "com.xzj.order.verification.mapper",
        "com.xzj.payment.mapper", "com.xzj.wallet.mapper"
})
@EnableFeignClients(basePackages = {"com.xzj.member", "com.xzj.config", "com.xzj.message"})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}

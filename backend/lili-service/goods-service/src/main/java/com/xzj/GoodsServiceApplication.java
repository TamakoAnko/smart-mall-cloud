package com.xzj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {
        "com.xzj.goods.category", "com.xzj.goods.goods", "com.xzj.goods.search",
        "com.xzj.goods.metrics", "com.xzj.goods.file", "com.xzj.goods.config",
        "com.xzj.goods.remote",
        "com.xzj.promotion", "com.xzj.store", "com.xzj.config",
        "cn.lili.common", "cn.lili.cache", "cn.lili.message", "cn.lili.mybatis",
        "cn.lili.rocketmq", "cn.lili.trigger"
})
@MapperScan({
        "com.xzj.goods.category.mapper", "com.xzj.goods.goods.mapper",
        "com.xzj.goods.search.mapper",
        "com.xzj.store.mapper"
})
@EnableFeignClients(basePackages = {"com.xzj.config", "com.xzj.member", "com.xzj.message", "com.xzj.order"})
public class GoodsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodsServiceApplication.class, args);
    }

}

package com.xzj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xzj.client")
public class LiliAiHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiliAiHelperApplication.class, args);
    }
}
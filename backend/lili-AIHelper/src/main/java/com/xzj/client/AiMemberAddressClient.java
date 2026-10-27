package com.xzj.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/** 收货地址：买家端 */
@FeignClient(name = "member-service", contextId = "aiMemberAddressClient", path = "/buyer/member/address")
public interface AiMemberAddressClient {

    @GetMapping("/get/default")
    JsonNode getDefaultAddress(@RequestHeader("accessToken") String accessToken);
}
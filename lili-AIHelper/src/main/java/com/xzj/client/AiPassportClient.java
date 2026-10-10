package com.xzj.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/** 当前登录会员：拿 memberId 用（买家端） */
@FeignClient(name = "member-service", contextId = "aiPassportClient", path = "/buyer/passport/member")
public interface AiPassportClient {

    @GetMapping
    JsonNode getCurrentMember(@RequestHeader("accessToken") String accessToken);
}
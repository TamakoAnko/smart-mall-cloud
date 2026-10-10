package com.xzj.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/** 订单：买家端 */
@FeignClient(name = "order-service", contextId = "aiOrderClient", path = "/buyer/order/order")
public interface AiOrderClient {

    /** 订单列表，orderStatus 可选（如 UNPAID/PAID/DELIVERED） */
    @GetMapping
    JsonNode listOrders(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                        @RequestParam(value = "pageSize", required = false) Integer pageSize,
                        @RequestParam(value = "orderStatus", required = false) String orderStatus,
                        @RequestHeader("accessToken") String accessToken);

    @GetMapping("/{orderSn}")
    JsonNode getOrder(@PathVariable("orderSn") String orderSn,
                      @RequestHeader("accessToken") String accessToken);

    @GetMapping("/getTracesList/{orderSn}")
    JsonNode getLogistics(@PathVariable("orderSn") String orderSn,
                          @RequestHeader("accessToken") String accessToken);

    @PostMapping("/{orderSn}/cancel")
    JsonNode cancel(@PathVariable("orderSn") String orderSn,
                    @RequestParam(value = "reason", required = false) String reason,
                    @RequestHeader("accessToken") String accessToken);
}
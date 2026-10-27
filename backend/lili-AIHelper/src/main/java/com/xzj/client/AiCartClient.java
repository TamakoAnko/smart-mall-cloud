package com.xzj.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/** 购物车 / 交易：买家端 */
@FeignClient(name = "order-service", contextId = "aiCartClient", path = "/buyer/trade/carts")
public interface AiCartClient {

    @GetMapping("/all")
    JsonNode cartAll(@RequestHeader("accessToken") String accessToken);

    @PostMapping
    JsonNode addToCart(@RequestParam("skuId") String skuId,
                       @RequestParam("num") Integer num,
                       @RequestHeader("accessToken") String accessToken);

    @PostMapping("/sku/num/{skuId}")
    JsonNode updateNum(@PathVariable("skuId") String skuId,
                       @RequestParam("num") Integer num,
                       @RequestHeader("accessToken") String accessToken);

    @DeleteMapping("/sku/remove")
    JsonNode remove(@RequestParam("skuIds") String skuIds,
                    @RequestHeader("accessToken") String accessToken);

    /** 结算页详情（选好地址/优惠券后能直接拿去创建交易） */
    @GetMapping("/checked")
    JsonNode checked(@RequestParam("way") String way,
                     @RequestHeader("accessToken") String accessToken);

    /** 选择收货地址 */
    @GetMapping("/shippingAddress")
    JsonNode selectAddress(@RequestParam("shippingAddressId") String shippingAddressId,
                           @RequestParam("way") String way,
                           @RequestHeader("accessToken") String accessToken);
}
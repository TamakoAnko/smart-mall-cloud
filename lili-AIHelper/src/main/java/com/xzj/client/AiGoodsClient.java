package com.xzj.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/** 商品：买家端 */
@FeignClient(name = "goods-service", contextId = "aiGoodsClient", path = "/buyer/goods/goods")
public interface AiGoodsClient {

    /** 商品搜索分页 */
    @GetMapping("/search")
    JsonNode searchGoods(@RequestParam("keyword") String keyword,
                         @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                         @RequestParam(value = "pageSize", required = false) Integer pageSize,
                         @RequestHeader("accessToken") String accessToken);

    /** 商品详情（返回的 result.skuList 里带 SKU 的价格和库存） */
    @GetMapping("/get/{goodsId}")
    JsonNode getGoods(@org.springframework.web.bind.annotation.PathVariable("goodsId") String goodsId,
                      @RequestHeader("accessToken") String accessToken);
}
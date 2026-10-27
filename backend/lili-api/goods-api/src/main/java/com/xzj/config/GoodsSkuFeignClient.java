package com.xzj.config;

import com.xzj.entity.GoodsSkuDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 商品服务内部查询接口。
 * 只传输会员服务展示足迹所需的 DTO，不跨服务共享商品实体和 Service。
 */
@FeignClient(name = "goods-service", contextId = "goodsSkuFeignClient", path = "/goods")
public interface GoodsSkuFeignClient {

    @GetMapping("/inner/sku")
    List<GoodsSkuDTO> findByIds(@RequestParam("ids") List<String> ids);

    @GetMapping("/inner/sku/store")
    List<GoodsSkuDTO> findByStoreId(@RequestParam("storeId") String storeId);

    @GetMapping("/inner/sku/goods")
    List<GoodsSkuDTO> findByGoodsId(@RequestParam("goodsId") String goodsId);

    @GetMapping("/inner/sku/{id}")
    GoodsSkuDTO findById(@PathVariable("id") String id);

    @PostMapping("/inner/sku/buy-count")
    Boolean updateBuyCount(@RequestParam("skuId") String skuId,
                           @RequestParam("buyCount") Integer buyCount);

    @GetMapping("/inner/sku/stock")
    Integer getStock(@RequestParam("skuId") String skuId);

    @PostMapping("/inner/sku/stock")
    Boolean updateStock(@RequestParam("skuId") String skuId,
                        @RequestParam("quantity") Integer quantity,
                        @RequestParam(value = "type", required = false) String type);
}

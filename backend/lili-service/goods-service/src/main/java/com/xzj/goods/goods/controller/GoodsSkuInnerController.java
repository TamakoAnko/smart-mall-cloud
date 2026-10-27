package com.xzj.goods.goods.controller;

import com.xzj.entity.GoodsSkuDTO;
import com.xzj.config.GoodsFeignClient;
import com.xzj.goods.goods.entity.GoodsSku;
import com.xzj.goods.goods.entity.GoodsSkuQueryParams;
import com.xzj.goods.goods.service.GoodsSkuQueryService;
import com.xzj.goods.goods.service.GoodsSkuService;
import com.xzj.goods.goods.service.GoodsService;
import com.xzj.goods.goods.entity.dos.Goods;
import com.xzj.goods.goods.entity.enums.GoodsStatusEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 商品服务内部接口，仅供其他微服务通过 Feign 调用。
 */
@RestController
@RequestMapping("/goods/inner")
public class GoodsSkuInnerController {

    private final GoodsSkuQueryService goodsSkuQueryService;
    private final GoodsSkuService goodsSkuService;
    private final GoodsService goodsService;

    public GoodsSkuInnerController(GoodsSkuQueryService goodsSkuQueryService, GoodsSkuService goodsSkuService,
                                   GoodsService goodsService) {
        this.goodsSkuQueryService = goodsSkuQueryService;
        this.goodsSkuService = goodsSkuService;
        this.goodsService = goodsService;
    }

    /** 批量查询 SKU，返回 API 层 DTO，避免跨服务依赖商品实体。 */
    @GetMapping("/sku")
    public List<GoodsSkuDTO> findByIds(@RequestParam("ids") List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        GoodsSkuQueryParams params = new GoodsSkuQueryParams();
        params.setSkuIds(ids);
        return goodsSkuQueryService.list(params).stream()
                .map(this::toDTO)
                .toList();
    }

    @GetMapping("/sku/store")
    public List<GoodsSkuDTO> findByStoreId(@RequestParam("storeId") String storeId) {
        if (storeId == null || storeId.isBlank()) {
            return Collections.emptyList();
        }
        GoodsSkuQueryParams params = new GoodsSkuQueryParams();
        params.setStoreId(storeId);
        return goodsSkuQueryService.list(params).stream().map(this::toDTO).toList();
    }

    @GetMapping("/sku/goods")
    public List<GoodsSkuDTO> findByGoodsId(@RequestParam("goodsId") String goodsId) {
        if (goodsId == null || goodsId.isBlank()) {
            return Collections.emptyList();
        }
        GoodsSkuQueryParams params = new GoodsSkuQueryParams();
        params.setGoodsId(goodsId);
        return goodsSkuQueryService.list(params).stream().map(this::toDTO).toList();
    }

    /** 查询单个 SKU，供评价等需要校验商品存在性的业务使用。 */
    @GetMapping("/sku/{id}")
    public GoodsSkuDTO findById(@PathVariable("id") String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        GoodsSkuQueryParams params = new GoodsSkuQueryParams();
        params.setSkuId(id);
        return goodsSkuQueryService.list(params).stream()
                .findFirst()
                .map(this::toDTO)
                .orElse(null);
    }
    private GoodsSkuDTO toDTO(GoodsSku sku) {
        GoodsSkuDTO dto = new GoodsSkuDTO();
        BeanUtils.copyProperties(sku, dto);
        return dto;
    }

    @PostMapping("/sku/buy-count")
    public Boolean updateBuyCount(@RequestParam("skuId") String skuId,
                                  @RequestParam("buyCount") Integer buyCount) {
        goodsSkuService.updateGoodsSkuBuyCount(skuId, buyCount == null ? 0 : buyCount);
        return true;
    }

    @GetMapping("/sku/stock")
    public Integer getStock(@RequestParam("skuId") String skuId) {
        return goodsSkuService.getStock(skuId);
    }

    @PostMapping("/sku/stock")
    public Boolean updateStock(@RequestParam("skuId") String skuId,
                               @RequestParam("quantity") Integer quantity,
                               @RequestParam(value = "type", required = false) String type) {
        if (type == null || type.isBlank()) {
            goodsSkuService.updateStock(skuId, quantity == null ? 0 : quantity);
        } else {
            goodsSkuService.updateStock(skuId, quantity == null ? 0 : quantity, type);
        }
        return true;
    }

    @GetMapping("/{id}")
    public GoodsFeignClient.GoodsSummaryDTO findGoodsById(@PathVariable("id") String id) {
        Goods goods = goodsService.getById(id);
        if (goods == null) return null;
        GoodsFeignClient.GoodsSummaryDTO dto = new GoodsFeignClient.GoodsSummaryDTO();
        dto.setId(goods.getId());
        dto.setBuyCount(goods.getBuyCount());
        dto.setGoodsName(goods.getGoodsName());
        dto.setCategoryPath(goods.getCategoryPath());
        dto.setStoreId(goods.getStoreId());
        dto.setStoreName(goods.getStoreName());
        dto.setAuthFlag(goods.getAuthFlag());
        dto.setMarketEnable(goods.getMarketEnable());
        return dto;
    }

    @PostMapping("/buy-count")
    public Boolean updateGoodsBuyCount(@RequestParam("goodsId") String goodsId,
                                       @RequestParam("buyCount") Integer buyCount) {
        goodsService.updateGoodsBuyCount(goodsId, buyCount == null ? 0 : buyCount);
        return true;
    }

    @PostMapping("/category-goods-name")
    public Boolean refreshCategoryGoodsName(@RequestParam("categoryId") String categoryId) {
        goodsService.categoryGoodsName(categoryId);
        return true;
    }

    @PostMapping("/market-status")
    public Boolean updateMarketStatus(@RequestParam("goodsIds") List<String> goodsIds,
                                      @RequestParam("status") String status,
                                      @RequestParam("reason") String reason) {
        goodsService.updateGoodsMarketAble(goodsIds, GoodsStatusEnum.valueOf(status), reason == null ? "" : reason);
        return true;
    }

    @PostMapping("/comment-count")
    public Boolean updateCommentCount(@RequestParam("goodsId") String goodsId,
                                      @RequestParam("skuId") String skuId) {
        goodsService.updateGoodsCommentNum(goodsId, skuId);
        return true;
    }
}

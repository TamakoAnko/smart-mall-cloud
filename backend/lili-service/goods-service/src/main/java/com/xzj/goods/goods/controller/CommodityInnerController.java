package com.xzj.goods.goods.controller;

import com.xzj.config.GoodsFeignClient;
import com.xzj.goods.goods.entity.dos.Commodity;
import com.xzj.goods.goods.service.CommodityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Internal live-commerce commodity projection for message-service. */
@RestController
@RequestMapping("/goods/inner/live")
public class CommodityInnerController {
    private final CommodityService commodityService;

    public CommodityInnerController(CommodityService commodityService) {
        this.commodityService = commodityService;
    }

    @GetMapping("/{roomId}/commodities")
    public List<GoodsFeignClient.CommoditySummaryDTO> getLiveCommodities(@PathVariable Integer roomId) {
        return commodityService.getCommodityByRoomId(roomId).stream().map(this::toSummary).toList();
    }

    @GetMapping("/{roomId}/commodity-names")
    public List<String> getLiveCommodityNames(@PathVariable Integer roomId) {
        return commodityService.getSimpleCommodityByRoomId(roomId);
    }

    private GoodsFeignClient.CommoditySummaryDTO toSummary(Commodity source) {
        GoodsFeignClient.CommoditySummaryDTO target = new GoodsFeignClient.CommoditySummaryDTO();
        target.setId(source.getId());
        target.setGoodsImage(source.getGoodsImage());
        target.setName(source.getName());
        target.setPriceType(source.getPriceType());
        target.setPrice(source.getPrice());
        target.setPrice2(source.getPrice2());
        target.setUrl(source.getUrl());
        target.setLiveGoodsId(source.getLiveGoodsId());
        target.setAuditId(source.getAuditId());
        target.setAuditStatus(source.getAuditStatus());
        target.setStoreId(source.getStoreId());
        target.setGoodsId(source.getGoodsId());
        target.setSkuId(source.getSkuId());
        return target;
    }
}

package com.xzj.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Internal goods operations used by event consumers. */
@FeignClient(name = "goods-service", contextId = "goodsFeignClient", path = "/goods")
public interface GoodsFeignClient {
    @GetMapping("/inner/{id}")
    GoodsSummaryDTO findById(@PathVariable("id") String id);

    @PostMapping("/inner/buy-count")
    Boolean updateBuyCount(@RequestParam("goodsId") String goodsId,
                           @RequestParam("buyCount") Integer buyCount);

    @PostMapping("/inner/category-goods-name")
    Boolean refreshCategoryGoodsName(@RequestParam("categoryId") String categoryId);

    @PostMapping("/inner/market-status")
    Boolean updateMarketStatus(@RequestParam("goodsIds") List<String> goodsIds,
                               @RequestParam("status") String status,
                               @RequestParam("reason") String reason);

    @PostMapping("/inner/comment-count")
    Boolean updateCommentCount(@RequestParam("goodsId") String goodsId,
                               @RequestParam("skuId") String skuId);

    @GetMapping("/inner/live/{roomId}/commodities")
    java.util.List<CommoditySummaryDTO> getLiveCommodities(@PathVariable("roomId") Integer roomId);

    @GetMapping("/inner/live/{roomId}/commodity-names")
    java.util.List<String> getLiveCommodityNames(@PathVariable("roomId") Integer roomId);

    class GoodsSummaryDTO {
        private String id;
        private Integer buyCount;
        private String goodsName;
        private String categoryPath;
        private String storeId;
        private String storeName;
        private String authFlag;
        private String marketEnable;

        public String getGoodsName() { return goodsName; }
        public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
        public String getCategoryPath() { return categoryPath; }
        public void setCategoryPath(String categoryPath) { this.categoryPath = categoryPath; }
        public String getStoreId() { return storeId; }
        public void setStoreId(String storeId) { this.storeId = storeId; }
        public String getStoreName() { return storeName; }
        public void setStoreName(String storeName) { this.storeName = storeName; }
        public String getAuthFlag() { return authFlag; }
        public void setAuthFlag(String authFlag) { this.authFlag = authFlag; }
        public String getMarketEnable() { return marketEnable; }
        public void setMarketEnable(String marketEnable) { this.marketEnable = marketEnable; }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Integer getBuyCount() { return buyCount; }
        public void setBuyCount(Integer buyCount) { this.buyCount = buyCount; }
    }

    class CommoditySummaryDTO {
        private String id;
        private String goodsImage;
        private String name;
        private Integer priceType;
        private Double price;
        private Double price2;
        private String url;
        private Integer liveGoodsId;
        private String auditId;
        private String auditStatus;
        private String storeId;
        private String goodsId;
        private String skuId;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getGoodsImage() { return goodsImage; }
        public void setGoodsImage(String value) { goodsImage = value; }
        public String getName() { return name; }
        public void setName(String value) { name = value; }
        public Integer getPriceType() { return priceType; }
        public void setPriceType(Integer value) { priceType = value; }
        public Double getPrice() { return price; }
        public void setPrice(Double value) { price = value; }
        public Double getPrice2() { return price2; }
        public void setPrice2(Double value) { price2 = value; }
        public String getUrl() { return url; }
        public void setUrl(String value) { url = value; }
        public Integer getLiveGoodsId() { return liveGoodsId; }
        public void setLiveGoodsId(Integer value) { liveGoodsId = value; }
        public String getAuditId() { return auditId; }
        public void setAuditId(String value) { auditId = value; }
        public String getAuditStatus() { return auditStatus; }
        public void setAuditStatus(String value) { auditStatus = value; }
        public String getStoreId() { return storeId; }
        public void setStoreId(String value) { storeId = value; }
        public String getGoodsId() { return goodsId; }
        public void setGoodsId(String value) { goodsId = value; }
        public String getSkuId() { return skuId; }
        public void setSkuId(String value) { skuId = value; }
    }
}

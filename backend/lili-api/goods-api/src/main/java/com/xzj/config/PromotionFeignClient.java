package com.xzj.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;
import java.util.Date;
import java.util.Map;

/** 商品服务内促销接口，优惠券数据归 goods-service 管理。 */
@FeignClient(name = "goods-service", contextId = "promotionFeignClient", path = "/promotion")
public interface PromotionFeignClient {
    @GetMapping("/inner/sku-promotion")
    Map<String, Object> getCurrentGoodsPromotion(@RequestParam("skuId") String skuId,
                                                   @RequestParam("cartType") String cartType);

    @GetMapping("/inner/wholesale")
    WholesaleDTO matchWholesale(@RequestParam("goodsId") String goodsId,
                                @RequestParam("num") Integer num);

    @GetMapping("/inner/member-coupon/list")
    List<MemberCouponDTO> getMemberCoupons(@RequestParam("memberId") String memberId);

    @GetMapping("/inner/member-coupon/live")
    List<MemberCouponDTO> getLiveMemberCoupons(@RequestParam("memberId") String memberId,
                                                @RequestParam("liveRoomId") String liveRoomId);

    @GetMapping("/inner/member-coupon/current")
    List<MemberCouponDTO> getCurrentGoodsCanUse(@RequestParam("memberId") String memberId,
                                                @RequestParam("couponIds") List<String> couponIds,
                                                @RequestParam("totalPrice") Double totalPrice);

    @GetMapping("/inner/member-coupon/scope")
    List<MemberCouponDTO> getAllScopeMemberCoupon(@RequestParam("memberId") String memberId,
                                                  @RequestParam("storeIds") List<String> storeIds);

    @GetMapping("/inner/member-coupon/one")
    MemberCouponDTO getMemberCoupon(@RequestParam("memberId") String memberId,
                                    @RequestParam("couponId") String couponId);
    @PostMapping("/inner/card-key/validate-trade")
    Boolean validateCardKeyTrade(@RequestParam("tradeJson") String tradeJson);

    @PostMapping("/inner/card-key/validate-pay")
    Boolean validateCardKeyPay(@RequestParam("orderSn") String orderSn);

    @org.springframework.web.bind.annotation.GetMapping("/inner/card-key/eligible")
    Boolean isCardKeyEligible(@RequestParam("orderSn") String orderSn);

    @PostMapping("/inner/card-key/fulfill")
    Boolean fulfillCardKey(@RequestParam("orderSn") String orderSn);

    @PostMapping("/inner/card-key/reserve")
    Boolean reserveCardKey(@RequestParam("orderSn") String orderSn);

    @PostMapping("/inner/card-key/confirm")
    Boolean confirmCardKey(@RequestParam("orderSn") String orderSn);

    @PostMapping("/inner/card-key/release")
    Boolean releaseCardKey(@RequestParam("orderSn") String orderSn);

    @PostMapping("/inner/card-key/enrich-detail")
    String enrichCardKeyOrderDetail(@RequestParam("detailJson") String detailJson);

    @PostMapping("/inner/kanjia/end")
    Boolean endKanjia(@RequestParam("kanjiaId") String kanjiaId);

    @PostMapping("/inner/gift-card/order-create")
    String applyGiftCardUsage(@RequestParam("tradeJson") String tradeJson,
                              @RequestParam("ordersJson") String ordersJson,
                              @RequestParam("orderItemsJson") String orderItemsJson);

    @PostMapping("/inner/gift-card/order-refund")
    Double resolveChannelRefundAfterGiftCard(@RequestParam("orderSn") String orderSn,
                                             @RequestParam("orderAmount") Double orderAmount);

    @PostMapping("/inner/gift-card/after-sale-refund")
    Double resolveChannelRefundAfterGiftCardForAfterSale(@RequestParam("afterSaleJson") String afterSaleJson);

    @GetMapping("/inner/pintuan/{pintuanId}")
    PintuanSummaryDTO getPintuan(@PathVariable("pintuanId") String pintuanId);

    class PintuanSummaryDTO implements Serializable {
        private String id;
        private Boolean fictitious;
        private Integer requiredNum;
        private java.util.Date startTime;
        private java.util.Date endTime;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public Boolean getFictitious() { return fictitious; }
        public void setFictitious(Boolean value) { fictitious = value; }
        public Integer getRequiredNum() { return requiredNum; }
        public void setRequiredNum(Integer value) { requiredNum = value; }
        public java.util.Date getStartTime() { return startTime; }
        public void setStartTime(java.util.Date value) { startTime = value; }
        public java.util.Date getEndTime() { return endTime; }
        public void setEndTime(java.util.Date value) { endTime = value; }
    }

    @GetMapping("/inner/coupon/{couponId}")
    CouponSummaryDTO getCoupon(@PathVariable("couponId") String couponId);

    @PostMapping("/inner/member-coupon/receive")
    Boolean receiveCoupon(@RequestParam("couponId") String couponId,
                          @RequestParam("memberId") String memberId,
                          @RequestParam("memberName") String memberName);

    @PostMapping("/inner/member-coupon/used")
    Boolean usedCoupons(@RequestParam("memberId") String memberId,
                        @RequestParam("ids") List<String> ids);

    class CouponSummaryDTO implements Serializable {
        private String id;
        private String couponName;
        private String couponType;
        private Double price;
        private Double couponDiscount;
        private String rangeDayType;
        private Integer effectiveDays;
        private Date startTime;
        private Date endTime;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getCouponName() { return couponName; }
        public void setCouponName(String value) { couponName = value; }
        public String getCouponType() { return couponType; }
        public void setCouponType(String value) { couponType = value; }
        public Double getPrice() { return price; }
        public void setPrice(Double value) { price = value; }
        public Double getCouponDiscount() { return couponDiscount; }
        public void setCouponDiscount(Double value) { couponDiscount = value; }
        public String getRangeDayType() { return rangeDayType; }
        public void setRangeDayType(String value) { rangeDayType = value; }
        public Integer getEffectiveDays() { return effectiveDays; }
        public void setEffectiveDays(Integer value) { effectiveDays = value; }
        public Date getStartTime() { return startTime; }
        public void setStartTime(Date value) { startTime = value; }
        public Date getEndTime() { return endTime; }
        public void setEndTime(Date value) { endTime = value; }
    }

    class WholesaleDTO implements Serializable {
        private Double price;
        public Double getPrice() { return price; }
        public void setPrice(Double value) { price = value; }
    }

    class MemberCouponDTO implements Serializable {
        private String id;
        private String couponId;
        private String memberId;
        private String storeId;
        private String storeName;
        private Double price;
        private Double discount;
        private Double consumeThreshold;
        private String couponType;
        private String scopeType;
        private String scopeId;
        private String memberCouponStatus;
        private Boolean platformFlag;
        private String liveRoomId;
        private Date startTime;
        private Date endTime;
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getCouponId() { return couponId; }
        public void setCouponId(String value) { couponId = value; }
        public String getMemberId() { return memberId; }
        public void setMemberId(String value) { memberId = value; }
        public String getStoreId() { return storeId; }
        public void setStoreId(String value) { storeId = value; }
        public String getStoreName() { return storeName; }
        public void setStoreName(String value) { storeName = value; }
        public Double getPrice() { return price; }
        public void setPrice(Double value) { price = value; }
        public Double getDiscount() { return discount; }
        public void setDiscount(Double value) { discount = value; }
        public Double getConsumeThreshold() { return consumeThreshold; }
        public void setConsumeThreshold(Double value) { consumeThreshold = value; }
        public String getCouponType() { return couponType; }
        public void setCouponType(String value) { couponType = value; }
        public String getScopeType() { return scopeType; }
        public void setScopeType(String value) { scopeType = value; }
        public String getScopeId() { return scopeId; }
        public void setScopeId(String value) { scopeId = value; }
        public String getMemberCouponStatus() { return memberCouponStatus; }
        public void setMemberCouponStatus(String value) { memberCouponStatus = value; }
        public Boolean getPlatformFlag() { return platformFlag; }
        public void setPlatformFlag(Boolean value) { platformFlag = value; }
        public String getLiveRoomId() { return liveRoomId; }
        public void setLiveRoomId(String value) { liveRoomId = value; }
        public Date getStartTime() { return startTime; }
        public void setStartTime(Date value) { startTime = value; }
        public Date getEndTime() { return endTime; }
        public void setEndTime(Date value) { endTime = value; }
    }
}

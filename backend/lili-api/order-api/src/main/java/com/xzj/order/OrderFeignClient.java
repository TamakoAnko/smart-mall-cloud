package com.xzj.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

/** Order queries and operations needed by other services. */
@FeignClient(name = "order-service", contextId = "orderFeignClient", path = "/order")
public interface OrderFeignClient {

    @GetMapping("/inner/item/{sn}")
    OrderItemSummaryDTO getOrderItemBySn(@PathVariable("sn") String sn);

    @GetMapping("/inner/{sn}/items")
    java.util.List<OrderItemSummaryDTO> getOrderItems(@PathVariable("sn") String sn);

    @GetMapping("/inner/gift-card/records/{orderSn}")
    java.util.List<GiftCardRecordDTO> getGiftCardRecords(@PathVariable("orderSn") String orderSn);

    @PostMapping("/inner/gift-card/records")
    Boolean saveGiftCardRecords(@RequestBody java.util.List<GiftCardRecordDTO> records);

    @GetMapping("/inner/gift-card/records/page")
    GiftCardUsageRecordPageDTO pageGiftCardRecords(
            @RequestParam(value = "orderSn", required = false) String orderSn,
            @RequestParam(value = "tradeSn", required = false) String tradeSn,
            @RequestParam(value = "memberId", required = false) String memberId,
            @RequestParam(value = "memberMobile", required = false) String memberMobile,
            @RequestParam(value = "memberName", required = false) String memberName,
            @RequestParam(value = "cardNo", required = false) String cardNo,
            @RequestParam(value = "flowType", required = false) String flowType,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "bizSn", required = false) String bizSn,
            @RequestParam(value = "transactionTimeStart", required = false) java.util.Date transactionTimeStart,
            @RequestParam(value = "transactionTimeEnd", required = false) java.util.Date transactionTimeEnd,
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size);

    @GetMapping("/inner/{sn}")
    OrderSummaryDTO getOrderBySn(@PathVariable("sn") String sn);

    @PostMapping("/inner/item/comment-status")
    Boolean updateCommentStatus(@RequestParam("orderItemSn") String orderItemSn,
                                @RequestParam("status") String status);

    @PostMapping("/inner/delivery")
    Boolean delivery(@RequestParam("orderSn") String orderSn,
                     @RequestParam("invoiceNumber") String invoiceNumber,
                     @RequestParam("logisticsId") String logisticsId);

    @GetMapping("/inner/promotion/list")
    java.util.List<OrderSummaryDTO> queryPromotionOrders(@RequestParam("promotionType") String promotionType,
                                                          @RequestParam("payStatus") String payStatus,
                                                          @RequestParam("parentOrderSn") String parentOrderSn,
                                                          @RequestParam("promotionId") String promotionId);

    @GetMapping("/inner/promotion/count")
    Long countPromotionOrders(@RequestParam("promotionType") String promotionType,
                               @RequestParam("payStatus") String payStatus,
                               @RequestParam("parentOrderSn") String parentOrderSn,
                               @RequestParam("promotionId") String promotionId);

    @PostMapping("/inner/promotion/fictitious")
    Boolean checkFictitiousOrder(@RequestParam("promotionId") String promotionId,
                                 @RequestParam("requiredNum") Integer requiredNum,
                                 @RequestParam("fictitious") Boolean fictitious);

    @PostMapping("/inner/ecoupon/complete")
    Boolean completeECoupon(@RequestParam("orderSn") String orderSn);

    @PostMapping("/inner/ecoupon/cancel")
    Boolean cancelECoupon(@RequestParam("orderSn") String orderSn,
                          @RequestParam("reason") String reason,
                          @RequestParam("refund") Boolean refund);

    @GetMapping("/inner/live/{liveRoomId}/stats")
    LiveOrderStatisticsDTO getLiveOrderStats(@PathVariable("liveRoomId") String liveRoomId);

    @GetMapping("/inner/live/{liveRoomId}")
    java.util.List<OrderSummaryDTO> getLiveOrders(@PathVariable("liveRoomId") String liveRoomId);

    class OrderItemSummaryDTO {
        private String sn;
        private String orderSn;
        private String skuId;
        private String commentStatus;
        private String goodsName;
        private Integer num;
        private Double giftCardPrice;

        public String getSn() { return sn; }
        public void setSn(String sn) { this.sn = sn; }
        public String getOrderSn() { return orderSn; }
        public void setOrderSn(String orderSn) { this.orderSn = orderSn; }
        public String getSkuId() { return skuId; }
        public void setSkuId(String skuId) { this.skuId = skuId; }
        public String getCommentStatus() { return commentStatus; }
        public void setCommentStatus(String commentStatus) { this.commentStatus = commentStatus; }
        public String getGoodsName() { return goodsName; }
        public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
        public Integer getNum() { return num; }
        public void setNum(Integer num) { this.num = num; }
        public Double getGiftCardPrice() { return giftCardPrice; }
        public void setGiftCardPrice(Double giftCardPrice) { this.giftCardPrice = giftCardPrice; }
    }

    class GiftCardRecordDTO implements java.io.Serializable {
        private String id;
        private String orderSn;
        private String tradeSn;
        private String memberId;
        private String credentialId;
        private String cardNo;
        private java.math.BigDecimal deductAmount;
        private java.math.BigDecimal changeAmount;
        private java.math.BigDecimal balanceAfter;
        private String flowType;
        private String bizType;
        private String bizSn;
        private Boolean deleteFlag;
        private java.util.Date createTime;
        private String memberName;
        private String memberMobile;
        private String giftCardName;
        private java.math.BigDecimal faceValue;
        private java.math.BigDecimal balance;

        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public String getOrderSn() { return orderSn; }
        public void setOrderSn(String value) { orderSn = value; }
        public String getTradeSn() { return tradeSn; }
        public void setTradeSn(String value) { tradeSn = value; }
        public String getMemberId() { return memberId; }
        public void setMemberId(String value) { memberId = value; }
        public String getCredentialId() { return credentialId; }
        public void setCredentialId(String value) { credentialId = value; }
        public String getCardNo() { return cardNo; }
        public void setCardNo(String value) { cardNo = value; }
        public java.math.BigDecimal getDeductAmount() { return deductAmount; }
        public void setDeductAmount(java.math.BigDecimal value) { deductAmount = value; }
        public java.math.BigDecimal getChangeAmount() { return changeAmount; }
        public void setChangeAmount(java.math.BigDecimal value) { changeAmount = value; }
        public java.math.BigDecimal getBalanceAfter() { return balanceAfter; }
        public void setBalanceAfter(java.math.BigDecimal value) { balanceAfter = value; }
        public String getFlowType() { return flowType; }
        public void setFlowType(String value) { flowType = value; }
        public String getBizType() { return bizType; }
        public void setBizType(String value) { bizType = value; }
        public String getBizSn() { return bizSn; }
        public void setBizSn(String value) { bizSn = value; }
        public Boolean getDeleteFlag() { return deleteFlag; }
        public void setDeleteFlag(Boolean value) { deleteFlag = value; }
        public java.util.Date getCreateTime() { return createTime; }
        public void setCreateTime(java.util.Date value) { createTime = value; }
        public String getMemberName() { return memberName; }
        public void setMemberName(String value) { memberName = value; }
        public String getMemberMobile() { return memberMobile; }
        public void setMemberMobile(String value) { memberMobile = value; }
        public String getGiftCardName() { return giftCardName; }
        public void setGiftCardName(String value) { giftCardName = value; }
        public java.math.BigDecimal getFaceValue() { return faceValue; }
        public void setFaceValue(java.math.BigDecimal value) { faceValue = value; }
        public java.math.BigDecimal getBalance() { return balance; }
        public void setBalance(java.math.BigDecimal value) { balance = value; }
    }

    class GiftCardUsageRecordPageDTO implements java.io.Serializable {
        private long current;
        private long size;
        private long total;
        private java.util.List<GiftCardRecordDTO> records;
        public long getCurrent() { return current; }
        public void setCurrent(long value) { current = value; }
        public long getSize() { return size; }
        public void setSize(long value) { size = value; }
        public long getTotal() { return total; }
        public void setTotal(long value) { total = value; }
        public java.util.List<GiftCardRecordDTO> getRecords() { return records; }
        public void setRecords(java.util.List<GiftCardRecordDTO> value) { records = value; }
    }

    class OrderSummaryDTO {
        private String sn;
        private String memberId;
        private String liveRoomId;
        private String storeId;
        private String storeName;
        private Double flowPrice;
        private String orderStatus;
        private String deliverStatus;
        private String consigneeName;
        private String consigneeMobile;
        private String consigneeAddressPath;
        private String consigneeDetail;
        private String remark;
        private String memberName;
        private String clientType;
        private String logisticsNo;
        private String logisticsName;
        private String paymentMethod;
        private java.util.Date logisticsTime;
        private String receivableNo;
        private String payOrderNo;
        private String promotionId;
        private String parentOrderSn;
        private String orderPromotionType;
        private String orderType;
        private String payStatus;
        private String distributionId;

        public String getSn() { return sn; }
        public void setSn(String sn) { this.sn = sn; }
        public String getMemberId() { return memberId; }
        public void setMemberId(String memberId) { this.memberId = memberId; }
        public String getLiveRoomId() { return liveRoomId; }
        public void setLiveRoomId(String liveRoomId) { this.liveRoomId = liveRoomId; }
        public String getStoreId() { return storeId; }
        public void setStoreId(String storeId) { this.storeId = storeId; }
        public String getStoreName() { return storeName; }
        public void setStoreName(String storeName) { this.storeName = storeName; }
        public Double getFlowPrice() { return flowPrice; }
        public void setFlowPrice(Double flowPrice) { this.flowPrice = flowPrice; }
        public String getOrderStatus() { return orderStatus; }
        public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
        public String getDeliverStatus() { return deliverStatus; }
        public void setDeliverStatus(String deliverStatus) { this.deliverStatus = deliverStatus; }
        public String getConsigneeName() { return consigneeName; }
        public void setConsigneeName(String consigneeName) { this.consigneeName = consigneeName; }
        public String getConsigneeMobile() { return consigneeMobile; }
        public void setConsigneeMobile(String consigneeMobile) { this.consigneeMobile = consigneeMobile; }
        public String getConsigneeAddressPath() { return consigneeAddressPath; }
        public void setConsigneeAddressPath(String consigneeAddressPath) { this.consigneeAddressPath = consigneeAddressPath; }
        public String getConsigneeDetail() { return consigneeDetail; }
        public void setConsigneeDetail(String consigneeDetail) { this.consigneeDetail = consigneeDetail; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public String getMemberName() { return memberName; }
        public void setMemberName(String memberName) { this.memberName = memberName; }
        public String getClientType() { return clientType; }
        public void setClientType(String clientType) { this.clientType = clientType; }
        public String getLogisticsNo() { return logisticsNo; }
        public void setLogisticsNo(String logisticsNo) { this.logisticsNo = logisticsNo; }
        public String getLogisticsName() { return logisticsName; }
        public void setLogisticsName(String logisticsName) { this.logisticsName = logisticsName; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public java.util.Date getLogisticsTime() { return logisticsTime; }
        public void setLogisticsTime(java.util.Date logisticsTime) { this.logisticsTime = logisticsTime; }
        public String getReceivableNo() { return receivableNo; }
        public void setReceivableNo(String receivableNo) { this.receivableNo = receivableNo; }
        public String getPayOrderNo() { return payOrderNo; }
        public void setPayOrderNo(String payOrderNo) { this.payOrderNo = payOrderNo; }
        public String getPromotionId() { return promotionId; }
        public void setPromotionId(String value) { promotionId = value; }
        public String getParentOrderSn() { return parentOrderSn; }
        public void setParentOrderSn(String value) { parentOrderSn = value; }
        public String getOrderPromotionType() { return orderPromotionType; }
        public void setOrderPromotionType(String value) { orderPromotionType = value; }
        public String getOrderType() { return orderType; }
        public void setOrderType(String value) { orderType = value; }
        public String getPayStatus() { return payStatus; }
        public void setPayStatus(String value) { payStatus = value; }
        public String getDistributionId() { return distributionId; }
        public void setDistributionId(String value) { distributionId = value; }
    }

    class LiveOrderStatisticsDTO {
        private Integer orderCount;
        private Integer orderUserCount;
        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
        public Integer getOrderUserCount() { return orderUserCount; }
        public void setOrderUserCount(Integer orderUserCount) { this.orderUserCount = orderUserCount; }
    }
}

package com.xzj.goods.remote.order;

import com.xzj.order.OrderFeignClient;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

/** Converts the order-service contract into goods-owned projections. */
@Service
public class OrderRemoteService {
    private final OrderFeignClient client;
    public OrderRemoteService(OrderFeignClient client) { this.client = client; }
    public Order getBySn(String sn) { return toOrder(client.getOrderBySn(sn)); }
    public List<OrderItem> getItems(String orderSn) { return client.getOrderItems(orderSn).stream().map(this::toItem).toList(); }
    public List<OrderGiftCardRecord> getGiftCardRecords(String orderSn) {
        return client.getGiftCardRecords(orderSn).stream().map(this::toRecord).toList();
    }
    public boolean saveGiftCardRecords(List<OrderGiftCardRecord> records) {
        return Boolean.TRUE.equals(client.saveGiftCardRecords(records == null ? Collections.emptyList() : records.stream()
                .map(this::toRemoteRecord).toList()));
    }
    public OrderFeignClient.GiftCardUsageRecordPageDTO pageGiftCardRecords(String orderSn, String tradeSn, String memberId,
                                                                             String memberMobile, String memberName,
                                                                             String cardNo, String flowType,
                                                                             String transactionType, String bizSn,
                                                                             java.util.Date transactionTimeStart,
                                                                             java.util.Date transactionTimeEnd,
                                                                             long current, long size) {
        return client.pageGiftCardRecords(orderSn, tradeSn, memberId, memberMobile, memberName, cardNo, flowType,
                transactionType, bizSn, transactionTimeStart, transactionTimeEnd, current, size);
    }
    public List<Order> getPromotionOrders(String type, String payStatus, String parentSn, String promotionId) {
        return client.queryPromotionOrders(type, payStatus, parentSn, promotionId).stream().map(this::toOrder).toList();
    }
    public boolean completeECoupon(String sn) { return Boolean.TRUE.equals(client.completeECoupon(sn)); }
    public boolean cancelECoupon(String sn, String reason, boolean refund) { return Boolean.TRUE.equals(client.cancelECoupon(sn, reason, refund)); }
    private Order toOrder(OrderFeignClient.OrderSummaryDTO source) {
        if (source == null) return null;
        Order result = new Order();
        result.setSn(source.getSn()); result.setMemberId(source.getMemberId()); result.setStoreId(source.getStoreId());
        result.setOrderStatus(source.getOrderStatus()); result.setPayStatus(source.getPayStatus());
        result.setOrderType(source.getOrderType()); result.setOrderPromotionType(source.getOrderPromotionType());
        result.setParentOrderSn(source.getParentOrderSn()); result.setFlowPrice(source.getFlowPrice());
        result.setCancelReason(source.getRemark());
        return result;
    }
    private OrderItem toItem(OrderFeignClient.OrderItemSummaryDTO source) {
        OrderItem result = new OrderItem();
        result.setSn(source.getSn()); result.setOrderSn(source.getOrderSn()); result.setSkuId(source.getSkuId());
        result.setGoodsName(source.getGoodsName()); result.setNum(source.getNum());
        PriceDetailDTO price = new PriceDetailDTO();
        price.setGiftCardPrice(source.getGiftCardPrice());
        result.setPriceDetailDTO(price);
        return result;
    }

    private OrderGiftCardRecord toRecord(OrderFeignClient.GiftCardRecordDTO source) {
        OrderGiftCardRecord result = new OrderGiftCardRecord();
        result.setId(source.getId());
        result.setOrderSn(source.getOrderSn());
        result.setTradeSn(source.getTradeSn());
        result.setMemberId(source.getMemberId());
        result.setCredentialId(source.getCredentialId());
        result.setCardNo(source.getCardNo());
        result.setDeductAmount(source.getDeductAmount());
        result.setChangeAmount(source.getChangeAmount());
        result.setBalanceAfter(source.getBalanceAfter());
        result.setFlowType(source.getFlowType());
        result.setBizType(source.getBizType());
        result.setBizSn(source.getBizSn());
        return result;
    }

    private OrderFeignClient.GiftCardRecordDTO toRemoteRecord(OrderGiftCardRecord source) {
        OrderFeignClient.GiftCardRecordDTO result = new OrderFeignClient.GiftCardRecordDTO();
        result.setId(source.getId());
        result.setOrderSn(source.getOrderSn());
        result.setTradeSn(source.getTradeSn());
        result.setMemberId(source.getMemberId());
        result.setCredentialId(source.getCredentialId());
        result.setCardNo(source.getCardNo());
        result.setDeductAmount(source.getDeductAmount());
        result.setChangeAmount(source.getChangeAmount());
        result.setBalanceAfter(source.getBalanceAfter());
        result.setFlowType(source.getFlowType());
        result.setBizType(source.getBizType());
        result.setBizSn(source.getBizSn());
        return result;
    }
}

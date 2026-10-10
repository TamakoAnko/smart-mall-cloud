package com.xzj.promotion.promotion.controller;

import cn.hutool.json.JSONUtil;
import com.xzj.config.PromotionFeignClient;
import com.xzj.goods.goods.service.CardKeyService;
import com.xzj.goods.remote.order.TradeDTO;
import com.xzj.goods.remote.order.Order;
import com.xzj.order.OrderFeignClient;
import com.xzj.promotion.promotion.service.KanjiaActivityService;
import com.xzj.promotion.promotion.entity.dos.Pintuan;
import com.xzj.promotion.promotion.service.PintuanService;
import com.xzj.goods.remote.order.OrderDetailVO;
import com.xzj.goods.remote.order.OrderItem;
import com.xzj.promotion.promotion.service.GiftCardCashService;
import com.xzj.goods.remote.order.AfterSale;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/promotion/inner")
public class PromotionOrderInnerController {
    private final CardKeyService cardKeyService;
    private final OrderFeignClient orderClient;
    private final KanjiaActivityService kanjiaActivityService;
    private final PintuanService pintuanService;
    private final GiftCardCashService giftCardCashService;

    public PromotionOrderInnerController(CardKeyService cardKeyService, OrderFeignClient orderClient,
                                          KanjiaActivityService kanjiaActivityService, PintuanService pintuanService,
                                          GiftCardCashService giftCardCashService) {
        this.cardKeyService = cardKeyService;
        this.orderClient = orderClient;
        this.kanjiaActivityService = kanjiaActivityService;
        this.pintuanService = pintuanService;
        this.giftCardCashService = giftCardCashService;
    }

    @GetMapping("/pintuan/{pintuanId}")
    public PromotionFeignClient.PintuanSummaryDTO getPintuan(@PathVariable String pintuanId) {
        Pintuan source = pintuanService.getById(pintuanId);
        if (source == null) return null;
        PromotionFeignClient.PintuanSummaryDTO target = new PromotionFeignClient.PintuanSummaryDTO();
        target.setId(source.getId()); target.setFictitious(source.getFictitious());
        target.setRequiredNum(source.getRequiredNum()); target.setStartTime(source.getStartTime()); target.setEndTime(source.getEndTime());
        return target;
    }

    @PostMapping("/card-key/validate-trade")
    public Boolean validateTrade(@RequestParam String tradeJson) {
        cardKeyService.validateTradeForECoupon(JSONUtil.toBean(tradeJson, TradeDTO.class));
        return true;
    }

    @PostMapping("/card-key/enrich-detail")
    public String enrichDetail(@RequestParam String detailJson) {
        OrderDetailVO detail = JSONUtil.toBean(detailJson, OrderDetailVO.class);
        cardKeyService.enrichOrderDetail(detail);
        return JSONUtil.toJsonStr(detail);
    }

    @PostMapping("/gift-card/order-create")
    public String orderCreate(@RequestParam String tradeJson, @RequestParam String ordersJson,
                              @RequestParam String orderItemsJson) {
        TradeDTO trade = JSONUtil.toBean(tradeJson, TradeDTO.class);
        List<Order> orders = JSONUtil.toList(ordersJson, Order.class);
        List<OrderItem> items = JSONUtil.toList(orderItemsJson, OrderItem.class);
        giftCardCashService.applyGiftCardUsageOnOrderCreate(trade, orders, items);
        Map<String, Object> result = new HashMap<>();
        result.put("orders", orders);
        result.put("orderItems", items);
        return JSONUtil.toJsonStr(result);
    }

    @PostMapping("/gift-card/order-refund")
    public Double orderRefund(@RequestParam String orderSn, @RequestParam Double orderAmount) {
        return giftCardCashService.resolveChannelRefundAfterGiftCard(orderSn, BigDecimal.valueOf(orderAmount)).doubleValue();
    }

    @PostMapping("/gift-card/after-sale-refund")
    public Double afterSaleRefund(@RequestParam String afterSaleJson) {
        return giftCardCashService.resolveChannelRefundAfterGiftCardForAfterSale(
                JSONUtil.toBean(afterSaleJson, AfterSale.class)).doubleValue();
    }

    @PostMapping("/card-key/validate-pay")
    public Boolean validatePay(@RequestParam String orderSn) {
        cardKeyService.validatePayOrder(toOrder(orderClient.getOrderBySn(orderSn)));
        return true;
    }

    @org.springframework.web.bind.annotation.GetMapping("/card-key/eligible")
    public Boolean eligible(@RequestParam String orderSn) {
        return cardKeyService.isECouponFulfillmentEligible(toOrder(orderClient.getOrderBySn(orderSn)));
    }

    @PostMapping("/card-key/fulfill")
    public Boolean fulfill(@RequestParam String orderSn) { cardKeyService.fulfillAfterPayment(orderSn); return true; }
    @PostMapping("/card-key/reserve")
    public Boolean reserve(@RequestParam String orderSn) { cardKeyService.reserveByOrder(orderSn); return true; }
    @PostMapping("/card-key/confirm")
    public Boolean confirm(@RequestParam String orderSn) { cardKeyService.confirmReservationAndComplete(orderSn); return true; }
    @PostMapping("/card-key/release")
    public Boolean release(@RequestParam String orderSn) { cardKeyService.releaseReservation(orderSn); return true; }
    @PostMapping("/kanjia/end")
    public Boolean endKanjia(@RequestParam String kanjiaId) { return kanjiaActivityService.endKanjiaActivity(kanjiaId); }

    private Order toOrder(OrderFeignClient.OrderSummaryDTO source) {
        if (source == null) return null;
        Order result = new Order();
        result.setSn(source.getSn());
        result.setMemberId(source.getMemberId());
        result.setStoreId(source.getStoreId());
        result.setOrderStatus(source.getOrderStatus());
        result.setPayStatus(source.getPayStatus());
        result.setOrderType(source.getOrderType());
        result.setOrderPromotionType(source.getOrderPromotionType());
        result.setParentOrderSn(source.getParentOrderSn());
        result.setCancelReason(source.getRemark());
        return result;
    }
}

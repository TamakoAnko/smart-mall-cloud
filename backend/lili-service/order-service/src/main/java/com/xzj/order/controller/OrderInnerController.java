package com.xzj.order.controller;

import com.xzj.order.OrderFeignClient;
import com.xzj.order.entity.dos.Order;
import com.xzj.order.entity.dos.OrderItem;
import com.xzj.order.entity.enums.CommentStatusEnum;
import com.xzj.order.service.OrderItemService;
import com.xzj.order.service.OrderService;
import com.xzj.order.mapper.OrderGiftCardRecordMapper;
import com.xzj.order.entity.dos.OrderGiftCardRecord;
import com.xzj.order.remote.GiftCardUsageRecordRemoteDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import com.xzj.order.entity.enums.PayStatusEnum;
import com.xzj.order.entity.dto.OrderSearchParams;

/** Internal order endpoints for service-to-service calls. */
@RestController
@RequestMapping("/order/inner")
public class OrderInnerController {
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderGiftCardRecordMapper orderGiftCardRecordMapper;

    public OrderInnerController(OrderService orderService, OrderItemService orderItemService,
                                OrderGiftCardRecordMapper orderGiftCardRecordMapper) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.orderGiftCardRecordMapper = orderGiftCardRecordMapper;
    }

    @GetMapping("/gift-card/records/{orderSn}")
    public List<OrderFeignClient.GiftCardRecordDTO> getGiftCardRecords(@PathVariable String orderSn) {
        return orderGiftCardRecordMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderGiftCardRecord>()
                        .eq(OrderGiftCardRecord::getOrderSn, orderSn)
                        .eq(OrderGiftCardRecord::getDeleteFlag, false)
                        .orderByAsc(OrderGiftCardRecord::getCreateTime)
                        .orderByAsc(OrderGiftCardRecord::getId))
                .stream().map(this::toGiftCardRecord).toList();
    }

    @PostMapping("/gift-card/records")
    public Boolean saveGiftCardRecords(@RequestBody List<OrderFeignClient.GiftCardRecordDTO> records) {
        if (records == null) {
            return true;
        }
        for (OrderFeignClient.GiftCardRecordDTO source : records) {
            if (source == null) {
                continue;
            }
            OrderGiftCardRecord target = new OrderGiftCardRecord();
            target.setId(source.getId());
            target.setOrderSn(source.getOrderSn());
            target.setTradeSn(source.getTradeSn());
            target.setMemberId(source.getMemberId());
            target.setCredentialId(source.getCredentialId());
            target.setCardNo(source.getCardNo());
            target.setDeductAmount(source.getDeductAmount());
            target.setChangeAmount(source.getChangeAmount());
            target.setBalanceAfter(source.getBalanceAfter());
            target.setFlowType(source.getFlowType());
            target.setBizType(source.getBizType());
            target.setBizSn(source.getBizSn());
            orderGiftCardRecordMapper.insert(target);
        }
        return true;
    }

    @GetMapping("/gift-card/records/page")
    public OrderFeignClient.GiftCardUsageRecordPageDTO pageGiftCardRecords(
            @RequestParam(required = false) String orderSn,
            @RequestParam(required = false) String tradeSn,
            @RequestParam(required = false) String memberId,
            @RequestParam(required = false) String memberMobile,
            @RequestParam(required = false) String memberName,
            @RequestParam(required = false) String cardNo,
            @RequestParam(required = false) String flowType,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String bizSn,
            @RequestParam(required = false) java.util.Date transactionTimeStart,
            @RequestParam(required = false) java.util.Date transactionTimeEnd,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        QueryWrapper<GiftCardUsageRecordRemoteDTO> wrapper = new QueryWrapper<>();
        wrapper.eq("ogr.delete_flag", false);
        if (org.springframework.util.StringUtils.hasText(orderSn)) wrapper.eq("ogr.order_sn", orderSn.trim());
        if (org.springframework.util.StringUtils.hasText(tradeSn)) wrapper.eq("ogr.trade_sn", tradeSn.trim());
        if (org.springframework.util.StringUtils.hasText(memberId)) wrapper.eq("ogr.member_id", memberId.trim());
        if (org.springframework.util.StringUtils.hasText(memberMobile)) wrapper.eq("m.mobile", memberMobile.trim());
        if (org.springframework.util.StringUtils.hasText(memberName)) {
            wrapper.and(q -> q.like("m.nick_name", memberName.trim()).or().like("m.username", memberName.trim()));
        }
        if (org.springframework.util.StringUtils.hasText(cardNo)) wrapper.like("ogr.card_no", cardNo.trim());
        if (org.springframework.util.StringUtils.hasText(flowType)) wrapper.eq("ogr.flow_type", flowType.trim());
        if (org.springframework.util.StringUtils.hasText(transactionType)) wrapper.eq("ogr.biz_type", transactionType.trim());
        if (org.springframework.util.StringUtils.hasText(bizSn)) wrapper.eq("ogr.biz_sn", bizSn.trim());
        if (transactionTimeStart != null) wrapper.ge("ogr.create_time", transactionTimeStart);
        if (transactionTimeEnd != null) wrapper.le("ogr.create_time", transactionTimeEnd);
        wrapper.orderByDesc("ogr.create_time");
        Page<GiftCardUsageRecordRemoteDTO> page = new Page<>(current, size);
        Page<GiftCardUsageRecordRemoteDTO> result = (Page<GiftCardUsageRecordRemoteDTO>) orderGiftCardRecordMapper.pageUsageRecord(page, wrapper);
        OrderFeignClient.GiftCardUsageRecordPageDTO response = new OrderFeignClient.GiftCardUsageRecordPageDTO();
        response.setCurrent(result.getCurrent());
        response.setSize(result.getSize());
        response.setTotal(result.getTotal());
        response.setRecords(result.getRecords().stream().map(this::toGiftCardRecord).toList());
        return response;
    }

    private OrderFeignClient.GiftCardRecordDTO toGiftCardRecord(OrderGiftCardRecord source) {
        OrderFeignClient.GiftCardRecordDTO target = new OrderFeignClient.GiftCardRecordDTO();
        target.setId(source.getId());
        target.setOrderSn(source.getOrderSn());
        target.setTradeSn(source.getTradeSn());
        target.setMemberId(source.getMemberId());
        target.setCredentialId(source.getCredentialId());
        target.setCardNo(source.getCardNo());
        target.setDeductAmount(source.getDeductAmount());
        target.setChangeAmount(source.getChangeAmount());
        target.setBalanceAfter(source.getBalanceAfter());
        target.setFlowType(source.getFlowType());
        target.setBizType(source.getBizType());
        target.setBizSn(source.getBizSn());
        target.setDeleteFlag(source.getDeleteFlag());
        target.setCreateTime(source.getCreateTime());
        return target;
    }

    private OrderFeignClient.GiftCardRecordDTO toGiftCardRecord(GiftCardUsageRecordRemoteDTO source) {
        OrderFeignClient.GiftCardRecordDTO target = new OrderFeignClient.GiftCardRecordDTO();
        target.setId(source.getId());
        target.setOrderSn(source.getOrderSn());
        target.setTradeSn(source.getTradeSn());
        target.setMemberId(source.getMemberId());
        target.setCredentialId(source.getCredentialId());
        target.setCardNo(source.getCardNo());
        target.setDeductAmount(source.getDeductAmount());
        target.setChangeAmount(source.getChangeAmount());
        target.setBalanceAfter(source.getBalanceAfter());
        target.setFlowType(source.getFlowType());
        target.setBizType(source.getBizType());
        target.setBizSn(source.getBizSn());
        target.setDeleteFlag(source.getDeleteFlag());
        target.setCreateTime(source.getCreateTime());
        target.setMemberName(source.getMemberName());
        target.setMemberMobile(source.getMemberMobile());
        target.setGiftCardName(source.getGiftCardName());
        target.setFaceValue(source.getFaceValue());
        target.setBalance(source.getBalance());
        return target;
    }

    @GetMapping("/item/{sn}")
    public OrderFeignClient.OrderItemSummaryDTO getOrderItemBySn(@PathVariable String sn) {
        OrderItem item = orderItemService.getBySn(sn);
        if (item == null) return null;
        OrderFeignClient.OrderItemSummaryDTO dto = new OrderFeignClient.OrderItemSummaryDTO();
        dto.setSn(item.getSn());
        dto.setOrderSn(item.getOrderSn());
        dto.setSkuId(item.getSkuId());
        dto.setCommentStatus(item.getCommentStatus());
        dto.setGoodsName(item.getGoodsName());
        dto.setNum(item.getNum());
        dto.setGiftCardPrice(item.getPriceDetailDTO() == null ? null : item.getPriceDetailDTO().getGiftCardPrice());
        return dto;
    }

    @GetMapping("/{sn}/items")
    public java.util.List<OrderFeignClient.OrderItemSummaryDTO> getOrderItems(@PathVariable String sn) {
        return orderItemService.getByOrderSn(sn).stream().map(item -> {
            OrderFeignClient.OrderItemSummaryDTO dto = new OrderFeignClient.OrderItemSummaryDTO();
            dto.setSn(item.getSn());
            dto.setOrderSn(item.getOrderSn());
            dto.setSkuId(item.getSkuId());
            dto.setCommentStatus(item.getCommentStatus());
            dto.setGoodsName(item.getGoodsName());
            dto.setNum(item.getNum());
            dto.setGiftCardPrice(item.getPriceDetailDTO() == null ? null : item.getPriceDetailDTO().getGiftCardPrice());
            return dto;
        }).toList();
    }

    @GetMapping("/{sn}")
    public OrderFeignClient.OrderSummaryDTO getOrderBySn(@PathVariable String sn) {
        Order order = orderService.getBySn(sn);
        if (order == null) return null;
        OrderFeignClient.OrderSummaryDTO dto = new OrderFeignClient.OrderSummaryDTO();
        dto.setSn(order.getSn());
        dto.setMemberId(order.getMemberId());
        dto.setStoreId(order.getStoreId());
        dto.setStoreName(order.getStoreName());
        dto.setFlowPrice(order.getFlowPrice());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setDeliverStatus(order.getDeliverStatus());
        dto.setConsigneeName(order.getConsigneeName());
        dto.setConsigneeMobile(order.getConsigneeMobile());
        dto.setConsigneeAddressPath(order.getConsigneeAddressPath());
        dto.setConsigneeDetail(order.getConsigneeDetail());
        dto.setRemark(order.getRemark());
        dto.setMemberName(order.getMemberName());
        dto.setClientType(order.getClientType());
        dto.setLogisticsNo(order.getLogisticsNo());
        dto.setLogisticsName(order.getLogisticsName());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setLogisticsTime(order.getLogisticsTime());
        dto.setReceivableNo(order.getReceivableNo());
        dto.setPayOrderNo(order.getPayOrderNo());
        dto.setPromotionId(order.getPromotionId());
        dto.setParentOrderSn(order.getParentOrderSn());
        dto.setOrderPromotionType(order.getOrderPromotionType());
        dto.setOrderType(order.getOrderType());
        dto.setPayStatus(order.getPayStatus());
        dto.setDistributionId(order.getDistributionId());
        return dto;
    }

    @GetMapping("/live/{liveRoomId}/stats")
    public OrderFeignClient.LiveOrderStatisticsDTO getLiveOrderStats(@PathVariable String liveRoomId) {
        List<Order> orders = orderService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .select(Order::getMemberId)
                .eq(Order::getLiveRoomId, liveRoomId)
                .eq(Order::getPayStatus, PayStatusEnum.PAID.name()));
        OrderFeignClient.LiveOrderStatisticsDTO dto = new OrderFeignClient.LiveOrderStatisticsDTO();
        dto.setOrderCount(orders.size());
        dto.setOrderUserCount((int) orders.stream().map(Order::getMemberId).distinct().count());
        return dto;
    }

    @GetMapping("/live/{liveRoomId}")
    public List<OrderFeignClient.OrderSummaryDTO> getLiveOrders(@PathVariable String liveRoomId) {
        return orderService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getLiveRoomId, liveRoomId)
                .eq(Order::getPayStatus, PayStatusEnum.PAID.name()))
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    private OrderFeignClient.OrderSummaryDTO toSummary(Order order) {
        OrderFeignClient.OrderSummaryDTO dto = new OrderFeignClient.OrderSummaryDTO();
        dto.setSn(order.getSn());
        dto.setMemberId(order.getMemberId());
        dto.setLiveRoomId(order.getLiveRoomId());
        dto.setMemberName(order.getMemberName());
        dto.setStoreId(order.getStoreId());
        dto.setStoreName(order.getStoreName());
        dto.setFlowPrice(order.getFlowPrice());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setDeliverStatus(order.getDeliverStatus());
        dto.setConsigneeName(order.getConsigneeName());
        dto.setConsigneeMobile(order.getConsigneeMobile());
        dto.setConsigneeAddressPath(order.getConsigneeAddressPath());
        dto.setConsigneeDetail(order.getConsigneeDetail());
        dto.setRemark(order.getRemark());
        dto.setClientType(order.getClientType());
        dto.setLogisticsNo(order.getLogisticsNo());
        dto.setLogisticsName(order.getLogisticsName());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setLogisticsTime(order.getLogisticsTime());
        dto.setReceivableNo(order.getReceivableNo());
        dto.setPayOrderNo(order.getPayOrderNo());
        return dto;
    }

    @PostMapping("/item/comment-status")
    public Boolean updateCommentStatus(@RequestParam String orderItemSn, @RequestParam String status) {
        orderItemService.updateCommentStatus(orderItemSn, CommentStatusEnum.valueOf(status));
        return true;
    }

    @PostMapping("/delivery")
    public Boolean delivery(@RequestParam String orderSn, @RequestParam String invoiceNumber,
                            @RequestParam String logisticsId) {
        orderService.delivery(orderSn, invoiceNumber, logisticsId);
        return true;
    }

    @GetMapping("/promotion/list")
    public List<OrderFeignClient.OrderSummaryDTO> queryPromotionOrders(@RequestParam String promotionType,
                                                                         @RequestParam String payStatus,
                                                                         @RequestParam String parentOrderSn,
                                                                         @RequestParam String promotionId) {
        OrderSearchParams params = new OrderSearchParams();
        params.setOrderPromotionType(promotionType);
        params.setPayStatus(payStatus);
        params.setParentOrderSn(parentOrderSn);
        params.setPromotionId(promotionId);
        return orderService.queryListByParams(params).stream().map(this::toSummary).toList();
    }

    @GetMapping("/promotion/count")
    public Long countPromotionOrders(@RequestParam String promotionType, @RequestParam String payStatus,
                                     @RequestParam String parentOrderSn, @RequestParam String promotionId) {
        return (long) queryPromotionOrders(promotionType, payStatus, parentOrderSn, promotionId).size();
    }

    @PostMapping("/promotion/fictitious")
    public Boolean checkFictitiousOrder(@RequestParam String promotionId, @RequestParam Integer requiredNum,
                                        @RequestParam Boolean fictitious) {
        return orderService.checkFictitiousOrder(promotionId, requiredNum, fictitious);
    }

    @PostMapping("/ecoupon/complete")
    public Boolean completeECoupon(@RequestParam String orderSn) {
        return orderService.systemCompleteECoupon(orderSn);
    }

    @PostMapping("/ecoupon/cancel")
    public Boolean cancelECoupon(@RequestParam String orderSn, @RequestParam String reason,
                                 @RequestParam Boolean refund) {
        return orderService.systemCancelECoupon(orderSn, reason, refund);
    }
}

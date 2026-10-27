package com.xzj.order.serviceimpl;

import cn.lili.common.utils.SnowFlake;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzj.order.aftersale.entity.dos.AfterSale;
import com.xzj.order.entity.dos.Order;
import com.xzj.order.entity.dos.OrderItem;
import com.xzj.order.entity.dos.StoreFlow;
import com.xzj.order.entity.enums.FlowTypeEnum;
import com.xzj.order.entity.enums.PayStatusEnum;
import com.xzj.order.mapper.StoreFlowMapper;
import com.xzj.order.service.OrderItemService;
import com.xzj.order.service.OrderService;
import com.xzj.order.service.StoreFlowService;
import com.xzj.payment.service.RefundLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Order-owned payment/refund flow records; settlement reporting is remote. */
@Slf4j
@Service("orderStoreFlowServiceImpl")
public class StoreFlowServiceImpl extends ServiceImpl<StoreFlowMapper, StoreFlow> implements StoreFlowService {
    @Lazy
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private RefundLogService refundLogService;

    @Override
    public void payOrder(String orderSn) {
        Order order = orderService.getBySn(orderSn);
        if (order == null) return;
        List<StoreFlow> flows = new ArrayList<>();
        for (OrderItem item : orderItemService.getByOrderSn(orderSn)) {
            flows.add(new StoreFlow(order, item, FlowTypeEnum.PAY));
        }
        if (!flows.isEmpty()) saveBatch(flows);
    }

    @Override
    public void orderCancel(String orderSn) {
        Order order = orderService.getBySn(orderSn);
        if (order == null || !PayStatusEnum.PAID.name().equals(order.getPayStatus())) return;
        update(new LambdaUpdateWrapper<StoreFlow>()
                .eq(StoreFlow::getOrderSn, orderSn)
                .set(StoreFlow::getBillTime, new Date()));
    }

    @Override
    public void refundOrder(AfterSale afterSale) {
        if (afterSale == null) return;
        StoreFlow flow = new StoreFlow();
        flow.setFlowType(FlowTypeEnum.REFUND.name());
        flow.setSn(SnowFlake.createStr("SF"));
        flow.setRefundSn(afterSale.getSn());
        flow.setOrderSn(afterSale.getOrderSn());
        flow.setOrderItemSn(afterSale.getOrderItemSn());
        flow.setStoreId(afterSale.getStoreId());
        flow.setStoreName(afterSale.getStoreName());
        flow.setMemberId(afterSale.getMemberId());
        flow.setMemberName(afterSale.getMemberName());
        flow.setGoodsId(afterSale.getGoodsId());
        flow.setGoodsName(afterSale.getGoodsName());
        flow.setSkuId(afterSale.getSkuId());
        flow.setImage(afterSale.getGoodsImage());
        flow.setSpecs(afterSale.getSpecs());
        flow.setNum(afterSale.getNum());
        flow.setFinalPrice(afterSale.getActualRefundPrice());
        var refundLog = refundLogService.queryByAfterSaleSn(afterSale.getSn());
        if (refundLog != null) {
            flow.setTransactionId(refundLog.getReceivableNo());
            flow.setPaymentName(refundLog.getPaymentName());
        }
        save(flow);
    }
}

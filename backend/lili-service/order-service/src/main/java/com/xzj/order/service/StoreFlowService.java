package com.xzj.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xzj.order.aftersale.entity.dos.AfterSale;
import com.xzj.order.entity.dos.StoreFlow;

/** Order-owned financial flow operations used during payment and refund. */
public interface StoreFlowService extends IService<StoreFlow> {
    void payOrder(String orderSn);
    void orderCancel(String orderSn);
    void refundOrder(AfterSale afterSale);
}

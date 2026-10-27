package com.xzj.goods.remote.order;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class OrderDetailVO implements Serializable {
    private Order order;
    private List<OrderItem> orderItems;
    private List<GiftECouponOrderSummaryVO> giftECouponOrders;
}

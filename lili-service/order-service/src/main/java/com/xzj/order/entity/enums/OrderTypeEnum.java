package com.xzj.order.entity.enums;

/**
 * 订单类型枚举
 *
 * @author Chopper
 * @since 2020/11/17 7:28 下午
 */
public enum OrderTypeEnum {

    /**
     * 普通订单
     */
    NORMAL,

    /**
     * 虚拟商品订单（{@link com.xzj.order.cart.entity.enums.CartTypeEnum#VIRTUAL} 等原有核销/提货码链路）
     */
    VIRTUAL,

    /**
     * 电子卡券订单（商品服务中的 E_COUPON 卡密商品，走卡池履约）
     */
    E_COUPON,
}


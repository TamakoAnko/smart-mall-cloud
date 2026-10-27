package com.xzj.order.cart.render;

import com.xzj.order.cart.entity.dto.TradeDTO;
import com.xzj.order.cart.entity.enums.RenderStepEnums;

/**
 * 购物车渲染
 *
 * @author Chopper
 * @since 2020-04-01 10:27 上午
 */
public interface CartRenderStep {


    /**
     * 渲染价格步骤
     *
     * @return 渲染枚举
     */
    RenderStepEnums step();

    /**
     * 渲染一笔交易
     *
     * @param tradeDTO 交易DTO
     */
    void render(TradeDTO tradeDTO);


}

package com.xzj.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.xzj.client.AiCartClient;
import org.springframework.stereotype.Component;

/** 执行「加入购物车」 */
@Component
public class AddToCartHandler implements PendingActionService.ActionHandler {

    private final AiCartClient cartClient;

    public AddToCartHandler(AiCartClient cartClient) {
        this.cartClient = cartClient;
    }

    @Override
    public String type() {
        return "ADD_TO_CART";
    }

    @Override
    public String execute(PendingActionService.PendingAction action, String accessToken) {
        String skuId = String.valueOf(action.args().get("skuId"));
        int num = ((Number) action.args().getOrDefault("num", 1)).intValue();
        JsonNode response = cartClient.addToCart(skuId, num, accessToken);
        if (response.path("success").asBoolean()) {
            return "已加入购物车：" + action.args().getOrDefault("goodsName", skuId) + " ×" + num;
        }
        return "加入购物车失败：" + response.path("message").asText();
    }
}
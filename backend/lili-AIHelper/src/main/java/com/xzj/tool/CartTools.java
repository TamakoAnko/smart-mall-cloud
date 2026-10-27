package com.xzj.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.xzj.action.PendingActionService;
import com.xzj.client.AiCartClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CartTools {

    private final AiCartClient cartClient;
    private final PendingActionService pendingActionService;

    public CartTools(AiCartClient cartClient, PendingActionService pendingActionService) {
        this.cartClient = cartClient;
        this.pendingActionService = pendingActionService;
    }

    @Tool(description = "查看当前用户购物车里的商品（只读）。")
    public String viewCart(ToolContext context) {
        JsonNode response = cartClient.cartAll(AiToolContext.accessToken(context));
        return response.path("success").asBoolean()
                ? "购物车内容：" + response.path("result").toString()
                : "查看购物车失败：" + response.path("message").asText();
    }

    @Tool(description = "把商品加入购物车（写操作，需要用户确认）。"
            + "本工具不会立即加购，而是生成一条待确认操作并返回 actionId 和操作摘要；"
            + "你必须把摘要念给用户听，并告诉用户在前端点击确认后才会真正加购。")
    public String prepareAddToCart(@ToolParam(description = "SKU编号，必须来自 getGoodsDetail") String skuId,
                                   @ToolParam(description = "数量，1~99", required = false) Integer num,
                                   @ToolParam(description = "商品名称，用于向用户复述") String goodsName,
                                   ToolContext context) {
        int quantity = num == null ? 1 : num;
        if (quantity < 1 || quantity > 99) {
            return "数量不合法，必须在 1~99 之间。";
        }
        Map<String, Object> args = new HashMap<>();
        args.put("skuId", skuId);
        args.put("num", quantity);
        args.put("goodsName", goodsName);

        PendingActionService.PendingAction action = pendingActionService.create(
                AiToolContext.memberId(context),
                "ADD_TO_CART",
                "把「" + goodsName + "」加入购物车 ×" + quantity,
                args);
        return "已生成待确认操作，actionId=" + action.id()
                + "，内容：" + action.summary()
                + "。请用户在前端确认后再继续。";
    }
}
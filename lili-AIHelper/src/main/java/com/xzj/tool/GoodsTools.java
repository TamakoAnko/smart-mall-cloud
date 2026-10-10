package com.xzj.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.xzj.client.AiGoodsClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 商品查询工具（只读）。
 * 小技巧：返回给模型的是「精简文本」而不是原始 JSON，既省 token 也更不容易让模型瞎编。
 */
@Component
public class GoodsTools {

    private final AiGoodsClient goodsClient;

    public GoodsTools(AiGoodsClient goodsClient) {
        this.goodsClient = goodsClient;
    }

    @Tool(description = "按关键词搜索商品。当用户想买东西但没给出商品ID时，先用这个工具搜索，"
            + "返回商品ID、名称、价格。参数 keyword 是关键词，例如「蓝牙耳机」「苹果手机壳」。")
    public String searchGoods(@ToolParam(description = "搜索关键词") String keyword,
                              @ToolParam(description = "页码，从1开始，默认1", required = false) Integer pageNumber,
                              ToolContext context) {
        JsonNode response = goodsClient.searchGoods(keyword, pageNumber == null ? 1 : pageNumber, 5,
                AiToolContext.accessToken(context));
        return formatSearchResult(response);
    }

    @Tool(description = "根据商品ID查商品详情，包含该商品下的 SKU 列表（skuId、规格、价格、库存）。"
            + "要给用户下单或加入购物车之前，必须先用它拿到 skuId。")
    public String getGoodsDetail(@ToolParam(description = "商品ID") String goodsId,
                                 ToolContext context) {
        JsonNode response = goodsClient.getGoods(goodsId, AiToolContext.accessToken(context));
        return formatGoodsDetail(response);
    }

    /** 商品搜索结果 -> 精简文本。字段名以真实返回为准，先自己用 Postman 调一次 /buyer/goods/goods/search 看结构。 */
    private String formatSearchResult(JsonNode response) {
        if (!response.path("success").asBoolean()) {
            return "搜索失败：" + response.path("message").asText();
        }
        JsonNode records = response.path("result").path("records");
        if (!records.isArray() || records.isEmpty()) {
            return "没有搜到相关商品，建议换个关键词，或者去首页看看分类。";
        }
        StringBuilder builder = new StringBuilder("搜索结果（最多5条）：\n");
        for (JsonNode item : records) {
            builder.append("- ")
                    .append(text(item, "goodsName", "name", "goods_name"))
                    .append(" | 商品ID=").append(text(item, "goodsId", "id"))
                    .append(" | 价格=").append(text(item, "price", "salePrice", "goodsPrice"))
                    .append('\n');
        }
        return builder.toString();
    }

    private String formatGoodsDetail(JsonNode response) {
        if (!response.path("success").asBoolean()) {
            return "查询商品失败：" + response.path("message").asText();
        }
        JsonNode goods = response.path("result");
        StringBuilder builder = new StringBuilder();
        builder.append("商品：").append(text(goods, "goodsName", "name")).append('\n');
        JsonNode skuList = goods.path("skuList");
        if (skuList.isArray() && !skuList.isEmpty()) {
            builder.append("可选规格（最多5个）：\n");
            int count = 0;
            for (JsonNode sku : skuList) {
                if (count++ >= 5) {
                    break;
                }
                builder.append("- skuId=").append(text(sku, "id", "skuId"))
                        .append(" | 规格=").append(text(sku, "simpleSpecs", "specs"))
                        .append(" | 价格=").append(text(sku, "price", "salePrice"))
                        .append(" | 库存=").append(text(sku, "quantity", "stock"))
                        .append('\n');
            }
        }
        return builder.toString();
    }

    private static String text(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.path(name);
            if (!value.isMissingNode() && !value.isNull() && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return "未知";
    }
}
package com.xzj.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.xzj.client.AiOrderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 订单查询工具（只读）。
 *
 * 踩过的坑：下游 OrderSearchParams.queryWrapper() 是
 *   wrapper.eq(CharSequenceUtil.isNotEmpty(orderStatus), "o.order_status", orderStatus)
 * 只要 orderStatus 非空就当精确值过滤，传 "全部"/"ALL"/中文 一律 0 条。
 * 所以模型给的原始字符串必须先白名单归一化。
 */
@Component
public class OrderTools {

    private static final Logger log = LoggerFactory.getLogger(OrderTools.class);

    /** OrderStatusEnum 里的真实值，一个都不能多、不能少 */
    private static final Set<String> VALID_STATUS = Set.of(
            "UNPAID", "PAID", "UNDELIVERED", "PARTS_DELIVERED", "DELIVERED",
            "COMPLETED", "STAY_PICKED_UP", "TAKE", "CANCELLED");

    private static final Map<String, String> STATUS_ALIAS = new LinkedHashMap<>();
    private static final Map<String, String> STATUS_TEXT = new LinkedHashMap<>();

    /** 表示「不限状态」的说法，一律映射成 null（不传给下游） */
    private static final Set<String> ALL_STATUS_WORDS = Set.of(
            "全部", "所有", "全部订单", "所有订单", "不限", "ALL", "ALL_ORDERS", "ANY", "NONE");

    static {
        STATUS_TEXT.put("UNPAID", "待付款");
        STATUS_TEXT.put("PAID", "已付款");
        STATUS_TEXT.put("UNDELIVERED", "待发货");
        STATUS_TEXT.put("PARTS_DELIVERED", "部分发货");
        STATUS_TEXT.put("DELIVERED", "已发货(待收货)");
        STATUS_TEXT.put("COMPLETED", "已完成");
        STATUS_TEXT.put("STAY_PICKED_UP", "待自提");
        STATUS_TEXT.put("TAKE", "已自提");
        STATUS_TEXT.put("CANCELLED", "已取消");

        VALID_STATUS.forEach(code -> STATUS_ALIAS.put(code, code));
        STATUS_TEXT.forEach((code, cn) -> STATUS_ALIAS.put(cn, code));
        STATUS_ALIAS.put("未付款", "UNPAID");
        STATUS_ALIAS.put("已支付", "PAID");
        STATUS_ALIAS.put("已关闭", "CANCELLED");
    }

    private final AiOrderClient orderClient;

    public OrderTools(AiOrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @Tool(description = """
            查询当前用户的订单列表（只读）。用户问「我的订单」「我买了什么」「订单到哪了」时使用。
            orderStatus 参数：
              - 用户说「全部订单 / 所有订单 / 我买了什么」时，必须不传（留空），不要填 "全部" 或 "ALL"；
              - 想按状态筛选时只能填这几个之一：UNPAID(待付款)、PAID(已付款)、UNDELIVERED(待发货)、
                DELIVERED(已发货)、COMPLETED(已完成)、CANCELLED(已取消)。
            如果带状态查出 0 条，要再用不带状态的方式查一次，不要直接告诉用户「你没有订单」。""")
    public String listMyOrders(@ToolParam(description = "订单状态，留空表示全部订单", required = false) String orderStatus,
                               @ToolParam(description = "页码，从1开始，默认1", required = false) Integer pageNumber,
                               ToolContext context) {
        String status = normalizeStatus(orderStatus);
        int page = (pageNumber == null || pageNumber < 1) ? 1 : pageNumber;

        JsonNode response = orderClient.listOrders(page, 10, status, AiToolContext.accessToken(context));
        if (!response.path("success").asBoolean()) {
            return "调用订单接口失败：" + response.path("message").asText();
        }

        JsonNode result = response.path("result");
        JsonNode records = result.path("records");
        long total = result.path("total").asLong(records.isArray() ? records.size() : 0);

        if (!records.isArray() || records.isEmpty()) {
            return status == null
                    ? "查询结果：未加状态过滤，该用户名下共 0 个订单。"
                    : "查询结果：加了状态过滤(" + status + ")后 0 条。这不等于用户没有订单，"
                    + "请用不带状态的方式再查一次确认。";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("查询结果：共 ").append(total).append(" 个订单，本页 ").append(records.size()).append(" 个\n");
        for (JsonNode order : records) {
            builder.append("- 订单号=").append(text(order, "sn"))
                    .append(" | 状态=").append(statusText(text(order, "orderStatus")))
                    .append(" | 金额=").append(text(order, "flowPrice"))
                    .append(" | 下单时间=").append(text(order, "createTime"))
                    .append(" | 店铺=").append(text(order, "storeName"))
                    .append(" | 商品=").append(goodsNames(order))
                    .append('\n');
        }
        return builder.toString();
    }

    @Tool(description = "根据订单号查询订单详情（状态、金额、收货人、地址、支付方式等）。订单号必须是用户提供或订单列表里查到的真实订单号。")
    public String getOrderDetail(@ToolParam(description = "订单号，例如 20250918000001") String orderSn,
                                 ToolContext context) {
        JsonNode response = orderClient.getOrder(orderSn, AiToolContext.accessToken(context));
        if (!response.path("success").asBoolean()) {
            return "没有查到该订单：" + response.path("message").asText();
        }
        // 注意：详情结构是 { order: {...}, orderItems: [...], orderStatusValue: "待发货" ... }
        JsonNode result = response.path("result");
        JsonNode order = result.path("order");
        if (order.isMissingNode() || order.isEmpty()) {
            return "没有查到该订单：" + response.path("message").asText();
        }
        return "订单号=" + text(order, "sn")
                + "\n状态=" + text(result, "orderStatusValue", "orderStatus")
                + "\n金额=" + text(order, "flowPrice")
                + "\n收货人=" + text(order, "consigneeName") + " " + text(order, "consigneeMobile")
                + "\n地址=" + text(order, "consigneeAddressPath") + text(order, "consigneeDetail")
                + "\n支付方式=" + text(result, "paymentMethodValue", "paymentMethod")
                + "\n物流单号=" + text(order, "logisticsNo")
                + "\n商品=" + goodsNames(result);
    }

    @Tool(description = "查询某个订单的物流轨迹。用户问「我的快递到哪了」「发货了吗」时使用。")
    public String getLogistics(@ToolParam(description = "订单号") String orderSn,
                               ToolContext context) {
        JsonNode response = orderClient.getLogistics(orderSn, AiToolContext.accessToken(context));
        if (!response.path("success").asBoolean()) {
            return "暂时查不到物流：" + response.path("message").asText();
        }
        JsonNode packages = response.path("result");
        if (!packages.isArray() || packages.isEmpty()) {
            return "这个订单还没有物流信息（可能还没发货）。";
        }
        StringBuilder builder = new StringBuilder("物流信息：\n");
        int packageCount = 0;
        for (JsonNode pkg : packages) {
            if (packageCount++ >= 3) {
                break;
            }
            builder.append("包裹").append(packageCount).append("：")
                    .append(text(pkg, "logisticsName")).append(" ").append(text(pkg, "logisticsNo")).append('\n');
            JsonNode traces = pkg.path("traces");
            if (traces.isArray()) {
                int i = 0;
                for (JsonNode trace : traces) {
                    if (i++ >= 3) {
                        break;
                    }
                    builder.append("  · ").append(text(trace, "time", "acceptTime"))
                            .append(" ").append(text(trace, "context", "message", "acceptStation")).append('\n');
                }
            }
        }
        return builder.toString();
    }

    /** 白名单归一化：认不出来就忽略过滤，避免「有订单却说没有」 */
    private String normalizeStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if (ALL_STATUS_WORDS.contains(trimmed) || ALL_STATUS_WORDS.contains(trimmed.toUpperCase())) {
            return null;
        }
        String mapped = STATUS_ALIAS.get(trimmed);
        if (mapped == null) {
            mapped = STATUS_ALIAS.get(trimmed.toUpperCase());
        }
        if (mapped == null) {
            log.warn("模型传入的订单状态无法识别，已忽略该过滤条件：{}", raw);
        }
        return mapped;
    }

    private String statusText(String code) {
        return STATUS_TEXT.getOrDefault(code, code);
    }

    /** 商品名在 orderItems 里，字段名 entity 是 goodsName、VO 是 name，两个都试一下 */
    private String goodsNames(JsonNode node) {
        JsonNode items = node.path("orderItems");
        if (!items.isArray() || items.isEmpty()) {
            return "（无商品明细）";
        }
        List<String> names = new ArrayList<>();
        for (JsonNode item : items) {
            if (names.size() >= 3) {
                break;
            }
            names.add(text(item, "name", "goodsName") + "×" + text(item, "num"));
        }
        return String.join("、", names);
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
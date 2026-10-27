package com.xzj.config;

import com.xzj.tool.CartTools;
import com.xzj.tool.GoodsTools;
import com.xzj.tool.OrderTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 配置：记忆（存 Redis）+ 本地 @Tool + MCP 工具 + 可选 RAG + 主 ChatClient。
 */
@Configuration
@EnableConfigurationProperties(MemoryProperties.class)
public class AiConfig {

    private static final String SYSTEM_PROMPT = """
            你是 lilishop 商城的 AI 助手，服务对象是买家。
            你可以调用工具帮用户查商品、查订单、查物流、看购物车，也可以读本地知识库里的商城规则文档。

            规则：
            1. 用中文、口语化、简短。
            2. 用户没给商品ID时先 searchGoods 搜索；要加购或下单必须先 getGoodsDetail 拿到 skuId。
            3. 只有工具真实返回的数据才能告诉用户，绝对不要自己编订单号、价格、库存、物流、政策条款。
            4. 涉及「加购物车/改数量/删除/下单/取消订单/确认收货」时，调用对应工具生成待确认操作，
               把操作内容复述给用户，请用户在前端确认，不要自己说"已经帮你下单了"。
            5. 用户问退换货、发货时效、优惠券规则、发票、售后这类问题时，先读知识库里的对应文档再回答，
               并说明依据来自哪份文档；知识库里没有的，就直说没有依据。
            6. 付款只能用户自己在收银台完成。
            """;

    /**
     * 窗口记忆：超过 maxMessages 条时丢掉最早的（兜底保护）。
     * maxMessages 必须大于 triggerMessages，否则窗口会先把老消息丢掉，摘要压缩永远触发不了。
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository, MemoryProperties properties) {
        int maxMessages = Math.max(properties.triggerMessages() * 2, properties.triggerMessages() + 10);
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(maxMessages)
                .build();
    }

    /**
     * 记忆 advisor：请求前把历史消息拼进 prompt，回复结束后把本轮问答写回记忆。
     * 会话 id 通过 ChatMemory.CONVERSATION_ID 从请求上下文传入（见 AiChatService）。
     */
    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 MessageChatMemoryAdvisor messageChatMemoryAdvisor,
                                 GoodsTools goodsTools,
                                 OrderTools orderTools,
                                 CartTools cartTools,
                                 ObjectProvider<ToolCallbackProvider> mcpToolCallbacks,
                                 ObjectProvider<RetrievalAugmentationAdvisor> ragAdvisor) {
        List<Advisor> advisors = new ArrayList<>();
        advisors.add(new SimpleLoggerAdvisor());
        advisors.add(messageChatMemoryAdvisor);
        // 只有打开 lili.ai.rag.enabled=true 时，容器里才有这个 advisor
        ragAdvisor.orderedStream().findFirst().ifPresent(advisors::add);

        ChatClient.Builder clientBuilder = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(advisors)
                .defaultTools(goodsTools, orderTools, cartTools);

        // MCP 工具（stdio/sse 服务器暴露的工具）；没配 MCP 时这里是空的
        List<ToolCallbackProvider> providers = mcpToolCallbacks.orderedStream().toList();
        if (!providers.isEmpty()) {
            clientBuilder.defaultToolCallbacks(providers.toArray(ToolCallbackProvider[]::new));
        }

        return clientBuilder.build();
    }
}

package com.xzj.memory;

import com.xzj.config.MemoryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 记忆摘要压缩：条数达到 trigger-messages 时，把「最近 keep-messages 条」以外的老消息
 * 交给大模型总结成 1 条系统消息，然后清空并重建这段记忆。
 *
 * 调用时机：每轮提问前（见 AiChatService.stream）。
 */
@Service
public class MemoryCompactionService {

    private static final Logger log = LoggerFactory.getLogger(MemoryCompactionService.class);

    private static final String SUMMARY_SYSTEM_PROMPT = """
            你是文本摘要助手。把历史对话压缩成要点，用中文，尽量简短：
            1. 用户的核心诉求与偏好；
            2. 已确认的关键信息（订单号、商品、地址、金额等）；
            3. 尚未解决的问题。
            只输出摘要正文，不要客套话。""";

    private static final String SUMMARY_PREFIX = "【历史对话摘要】";

    private final ChatMemory chatMemory;
    private final MemoryProperties properties;
    private final ChatClient summarizer;

    public MemoryCompactionService(ChatMemory chatMemory, ChatModel chatModel, MemoryProperties properties) {
        this.chatMemory = chatMemory;
        this.properties = properties;
        // 摘要用的客户端不挂记忆 advisor，避免「压缩时又触发压缩」的递归
        this.summarizer = ChatClient.builder(chatModel)
                .defaultSystem(SUMMARY_SYSTEM_PROMPT)
                .build();
    }

    public void compactIfNeeded(String conversationId) {
        List<Message> history = chatMemory.get(conversationId);
        if (history.size() < properties.triggerMessages()) {
            return;
        }
        int keep = Math.min(properties.keepMessages(), history.size());
        List<Message> oldMessages = new ArrayList<>(history.subList(0, history.size() - keep));
        if (oldMessages.isEmpty()) {
            return;
        }
        try {
            String summary = summarize(oldMessages);
            if (summary == null || summary.isBlank()) {
                log.warn("会话[{}]摘要为空，本次不压缩", conversationId);
                return;
            }
            List<Message> compacted = new ArrayList<>(keep + 1);
            compacted.add(new SystemMessage(SUMMARY_PREFIX + summary));
            compacted.addAll(history.subList(history.size() - keep, history.size()));

            chatMemory.clear(conversationId);
            chatMemory.add(conversationId, compacted);
            log.info("会话[{}]记忆已压缩：{} 条 -> {} 条", conversationId, history.size(), compacted.size());
        } catch (Exception e) {
            // 压缩失败不能影响正常对话：保持原记忆，只记日志
            log.warn("会话[{}]记忆压缩失败，保持原样：{}", conversationId, e.getMessage());
        }
    }

    private String summarize(List<Message> messages) {
        String dialogue = messages.stream()
                .map(message -> message.getMessageType().getValue() + ": "
                        + (message.getText() == null ? "" : message.getText()))
                .collect(Collectors.joining("\n"));
        return summarizer.prompt()
                .user("请总结下面这段历史对话：\n" + dialogue)
                .call()
                .content();
    }
}

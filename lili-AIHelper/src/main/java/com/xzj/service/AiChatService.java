package com.xzj.service;

import com.xzj.dto.ChatMessageView;
import com.xzj.memory.MemoryCompactionService;
import com.xzj.tool.AiToolContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI 对话服务：会话 id 生成与归属校验、压缩触发、流式对话、历史查询。
 */
@Service
public class AiChatService {

    private static final String SEPARATOR = ":";

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final MemoryCompactionService compactionService;

    public AiChatService(ChatClient chatClient,
                         ChatMemory chatMemory,
                         MemoryCompactionService compactionService) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.compactionService = compactionService;
    }

    /** 会话 id 里带归属：别人拿到 id 也用不了（userId 不能含 ':'） */
    public String createConversation(String userId) {
        return userId + SEPARATOR + UUID.randomUUID().toString().replace("-", "");
    }

    public Flux<String> stream(String conversationId, String memberId, String accessToken, String message) {
        assertOwner(conversationId, memberId);
        compactionService.compactIfNeeded(conversationId);
        return chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .toolContext(Map.of(
                        AiToolContext.ACCESS_TOKEN, accessToken,
                        AiToolContext.MEMBER_ID, memberId,
                        AiToolContext.CONVERSATION_ID, conversationId))
                .user(message)
                .stream()
                .content();
    }

    public List<ChatMessageView> history(String conversationId, String userId) {
        assertOwner(conversationId, userId);
        return chatMemory.get(conversationId).stream().map(ChatMessageView::from).toList();
    }

    public void clear(String conversationId, String userId) {
        assertOwner(conversationId, userId);
        chatMemory.clear(conversationId);
    }

    private void assertOwner(String conversationId, String userId) {
        if (conversationId == null || !conversationId.startsWith(userId + SEPARATOR)) {
            throw new IllegalArgumentException("会话不存在或不属于当前用户");
        }
    }
}
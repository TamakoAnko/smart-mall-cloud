package com.xzj.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xzj.config.MemoryProperties;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Redis 版对话记忆仓库：一个会话一个 String 键，值是消息数组的 JSON。
 * 键格式：lili:ai:memory:{conversationId}
 *
 * 说明：只落「谁说的 + 文本」，够商城主流程用；多模态附件和工具调用不落库。
 */
@Repository
public class RedisChatMemoryRepository implements ChatMemoryRepository {

    private static final String KEY_PREFIX = "lili:ai:memory:";
    private static final String SCAN_PATTERN = KEY_PREFIX + "*";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final MemoryProperties properties;

    public RedisChatMemoryRepository(StringRedisTemplate redis,
                                     ObjectMapper objectMapper,
                                     MemoryProperties properties) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public List<String> findConversationIds() {
        List<String> ids = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(SCAN_PATTERN).count(200).build();
        try (Cursor<String> cursor = redis.scan(options)) {
            while (cursor.hasNext()) {
                ids.add(cursor.next().substring(KEY_PREFIX.length()));
            }
        }
        return ids;
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        String json = redis.opsForValue().get(key(conversationId));
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            StoredMessage[] stored = objectMapper.readValue(json, StoredMessage[].class);
            return Arrays.stream(stored).map(StoredMessage::toMessage).toList();
        } catch (Exception e) {
            throw new IllegalStateException("读取会话记忆失败: " + conversationId, e);
        }
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        try {
            List<StoredMessage> stored = messages.stream().map(StoredMessage::from).toList();
            redis.opsForValue().set(key(conversationId), objectMapper.writeValueAsString(stored), properties.ttl());
        } catch (Exception e) {
            throw new IllegalStateException("保存会话记忆失败: " + conversationId, e);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        redis.delete(key(conversationId));
    }

    private String key(String conversationId) {
        return KEY_PREFIX + conversationId;
    }

    /** 落库用的最小结构 */
    private record StoredMessage(String type, String text) {

        static StoredMessage from(Message message) {
            String text = message.getText() == null ? "" : message.getText();
            return new StoredMessage(message.getMessageType().getValue(), text);
        }

        Message toMessage() {
            String content = text == null ? "" : text;
            return switch (MessageType.fromValue(type)) {
                case USER -> new UserMessage(content);
                case ASSISTANT -> new AssistantMessage(content);
                case SYSTEM -> new SystemMessage(content);
                // 工具返回消息还需要 toolCallId 才能还原，这里降级成助手文本，保证历史不断裂
                case TOOL -> new AssistantMessage(content);
            };
        }
    }
}

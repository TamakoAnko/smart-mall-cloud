package com.xzj.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 待确认操作：模型想写数据时，先在这里生成一条记录，等用户在前端点「确认」才真正执行。
 * 好处：① 模型不能自己把订单/购物车改了；② 参数能原样展示给用户复核；③ 天然带幂等（一条记录只能用一次）。
 */
@Service
public class PendingActionService {

    private static final String KEY_PREFIX = "lili:ai:pending:";
    private static final Duration TTL = Duration.ofMinutes(10);

    public record PendingAction(String id, String memberId, String type, String summary, Map<String, Object> args) {
    }

    public interface ActionHandler {
        String type();

        String execute(PendingAction action, String accessToken);
    }

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Map<String, ActionHandler> handlers;

    public PendingActionService(StringRedisTemplate redis, ObjectMapper objectMapper, List<ActionHandler> handlers) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.handlers = handlers.stream().collect(Collectors.toMap(ActionHandler::type, Function.identity()));
    }

    public PendingAction create(String memberId, String type, String summary, Map<String, Object> args) {
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        PendingAction action = new PendingAction(id, memberId, type, summary, args);
        try {
            redis.opsForValue().set(KEY_PREFIX + id, objectMapper.writeValueAsString(action), TTL);
        } catch (Exception e) {
            throw new IllegalStateException("创建待确认操作失败", e);
        }
        return action;
    }

    /** 用户点确认后才走这里 */
    public String confirm(String memberId, String accessToken, String actionId) {
        String json = redis.opsForValue().get(KEY_PREFIX + actionId);
        if (json == null) {
            throw new IllegalArgumentException("这个操作已经过期或不存在了，请重新发起");
        }
        try {
            PendingAction action = objectMapper.readValue(json, PendingAction.class);
            if (!action.memberId().equals(memberId)) {
                throw new IllegalArgumentException("无权执行该操作");
            }
            ActionHandler handler = handlers.get(action.type());
            if (handler == null) {
                throw new IllegalStateException("不支持的操作类型：" + action.type());
            }
            String result = handler.execute(action, accessToken);
            redis.delete(KEY_PREFIX + actionId);   // 一次性，防重复提交
            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("执行操作失败", e);
        }
    }
}
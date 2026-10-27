package com.xzj.tool;

import org.springframework.ai.chat.model.ToolContext;

/**
 * 工具执行上下文里的公共字段。
 * 为什么不用 UserContext（lili-common 那个）：它是基于 HttpServletRequest + ThreadLocal 的，
 * AIHelper 里工具执行线程不保证是请求线程，所以显式通过 ToolContext 传。
 */
public final class AiToolContext {

    public static final String ACCESS_TOKEN = "accessToken";
    public static final String MEMBER_ID = "memberId";
    public static final String CONVERSATION_ID = "conversationId";

    private AiToolContext() {
    }

    public static String accessToken(ToolContext context) {
        Object value = context == null ? null : context.getContext().get(ACCESS_TOKEN);
        if (!(value instanceof String token) || token.isBlank()) {
            throw new IllegalStateException("当前会话缺少登录令牌，请让用户先登录后再试");
        }
        return token;
    }

    public static String memberId(ToolContext context) {
        Object value = context == null ? null : context.getContext().get(MEMBER_ID);
        return value instanceof String memberId ? memberId : "anonymous";
    }
}
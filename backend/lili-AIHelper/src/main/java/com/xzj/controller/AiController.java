package com.xzj.controller;

import com.xzj.action.PendingActionService;
import com.xzj.dto.ActionResultView;
import com.xzj.dto.ChatMessageView;
import com.xzj.dto.ChatRequest;
import com.xzj.dto.ConversationView;
import com.xzj.service.AiChatService;
import com.xzj.service.AiIdentityService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/ai")
public class AiController {

    /** 网关透传的登录令牌头，名字要和 SecurityEnum.HEADER_TOKEN 一致 */
    private static final String TOKEN_HEADER = "accessToken";

    private final AiChatService aiChatService;
    private final AiIdentityService identityService;
    private final PendingActionService pendingActionService;

    public AiController(AiChatService aiChatService,
                        AiIdentityService identityService,
                        PendingActionService pendingActionService) {
        this.aiChatService = aiChatService;
        this.identityService = identityService;
        this.pendingActionService = pendingActionService;
    }

    /** 建会话：用 token 换 memberId，会话 id 形如 "1001:8f3b..." */
    @PostMapping("/conversations")
    public ConversationView createConversation(
            @RequestHeader(value = TOKEN_HEADER, required = false) String accessToken) {
        String memberId = identityService.resolveMemberId(accessToken);
        return new ConversationView(aiChatService.createConversation(memberId));
    }

    /** 流式对话（SSE） */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@Valid @RequestBody ChatRequest request,
                             @RequestHeader(value = TOKEN_HEADER, required = false) String accessToken) {
        String memberId = identityService.resolveMemberId(accessToken);
        return aiChatService.stream(request.conversationId(), memberId, accessToken, request.message());
    }

    /** 查看某个会话的记忆（含压缩后的摘要），调试用 */
    @GetMapping("/conversations/{conversationId}/messages")
    public List<ChatMessageView> history(@PathVariable("conversationId") String conversationId,
                                         @RequestHeader(value = TOKEN_HEADER, required = false) String accessToken) {
        return aiChatService.history(conversationId, identityService.resolveMemberId(accessToken));
    }

    /** 清空某个会话的记忆 */
    @DeleteMapping("/conversations/{conversationId}")
    public void clear(@PathVariable("conversationId") String conversationId,
                      @RequestHeader(value = TOKEN_HEADER, required = false) String accessToken) {
        aiChatService.clear(conversationId, identityService.resolveMemberId(accessToken));
    }

    /** 确认执行 AI 生成的写操作（加购物车等），一次性消费 */
    @PostMapping("/actions/{actionId}/confirm")
    public ActionResultView confirmAction(@PathVariable("actionId") String actionId,
                                          @RequestHeader(value = TOKEN_HEADER, required = false) String accessToken) {
        String memberId = identityService.resolveMemberId(accessToken);
        return new ActionResultView(pendingActionService.confirm(memberId, accessToken, actionId));
    }
}
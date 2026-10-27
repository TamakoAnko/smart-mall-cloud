<template>
  <div class="ai-assistant">
    <!-- 悬浮入口 -->
    <div class="ai-bubble" :class="{ 'is-open': visible }" @click="toggle">
      <el-icon :size="22"><ChatDotRound /></el-icon>
      <span v-if="!visible" class="ai-bubble-text">AI助手</span>
    </div>

    <!-- 对话面板 -->
    <transition name="ai-slide">
      <div v-show="visible" class="ai-panel">
        <div class="ai-header">
          <div class="ai-header-title">
            <el-icon :size="18"><MagicStick /></el-icon>
            <span>AI 助手</span>
          </div>
          <div class="ai-header-tools">
            <el-tooltip content="开启新会话" placement="bottom">
              <el-icon :size="16" @click="newConversation"><Plus /></el-icon>
            </el-tooltip>
            <el-tooltip content="清空记忆" placement="bottom">
              <el-icon :size="16" @click="clearAll"><Delete /></el-icon>
            </el-tooltip>
            <el-tooltip content="关闭" placement="bottom">
              <el-icon :size="16" @click="toggle"><Close /></el-icon>
            </el-tooltip>
          </div>
        </div>

        <div ref="bodyEl" class="ai-body">
          <!-- 未登录 -->
          <div v-if="needLogin" class="ai-notice">
            <p>AI 助手需要先登录才能查订单、看购物车。</p>
            <el-button type="primary" size="small" @click="goLogin">去登录</el-button>
          </div>

          <!-- 空状态 -->
          <div v-else-if="messages.length === 0" class="ai-welcome">
            <div class="ai-welcome-avatar">
              <el-icon :size="26"><ChatDotRound /></el-icon>
            </div>
            <p class="ai-welcome-title">你好，我是 lilishop 智能助手</p>
            <p class="ai-welcome-desc">可以帮你找商品、查订单、看物流、逛购物车</p>
            <div class="ai-quick">
              <span
                v-for="(item, index) in quickPrompts"
                :key="index"
                class="ai-quick-item"
                @click="quickAsk(item)"
              >{{ item }}</span>
            </div>
          </div>

          <!-- 消息列表 -->
          <div
            v-for="item in messages"
            :key="item.id"
            class="ai-msg"
            :class="item.role === 'user' ? 'ai-msg-user' : 'ai-msg-ai'"
          >
            <div class="ai-avatar">
              <el-icon :size="16">
                <component :is="item.role === 'user' ? 'User' : 'ChatDotRound'" />
              </el-icon>
            </div>
            <div class="ai-msg-main">
              <div
                class="ai-bubble-text"
                :class="{ 'is-error': item.failed }"
                v-html="renderText(item)"
              ></div>
              <span v-if="item.streaming" class="ai-typing">
                <i></i><i></i><i></i>
              </span>

              <!-- 需要用户确认的写操作 -->
              <div v-if="item.actionId" class="ai-action">
                <template v-if="item.actionState === 'done'">
                  <el-icon class="ai-action-icon is-done"><CircleCheck /></el-icon>
                  <span>{{ item.actionResult }}</span>
                </template>
                <template v-else>
                  <span class="ai-action-tip">这是写操作，确认后才会真正执行</span>
                  <el-button
                    type="primary"
                    size="small"
                    :loading="item.actionState === 'loading'"
                    @click="doConfirm(item)"
                  >确认执行</el-button>
                  <el-button size="small" @click="item.actionId = ''">取消</el-button>
                </template>
              </div>
            </div>
          </div>
        </div>

        <div class="ai-footer">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            resize="none"
            maxlength="2000"
            placeholder="问问商品、订单、物流…（Enter 发送，Shift+Enter 换行）"
            @keydown.enter="onEnter"
          />
          <div class="ai-footer-bar">
            <span class="ai-footer-hint">AI 可能会出错，重要信息请以页面为准</span>
            <el-button v-if="sending" size="small" @click="stop">停止</el-button>
            <el-button
              v-else
              type="primary"
              size="small"
              :disabled="!input.trim()"
              @click="send"
            >发送</el-button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import {
  ChatDotRound,
  Close,
  Delete,
  Plus,
  CircleCheck,
  MagicStick,
  User,
} from "@element-plus/icons-vue";
import storage from "@/plugins/storage";
import { Message } from "@/utils/message";
import {
  createConversation,
  streamChat,
  confirmAction,
  clearConversation,
  getHistory,
} from "@/api/aiHelper";

const CONVERSATION_KEY = "aiConversationId";

/** 从模型回复里抠出待确认操作的 actionId（后端 CartTools 返回 "actionId=xxxxxxxxxxxx"） */
function extractActionId(text) {
  const matched = /actionId\s*[=:：]\s*([A-Za-z0-9_-]{6,40})/.exec(text || "");
  return matched ? matched[1] : "";
}

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

/** 只做最基础的排版（粗体/行内代码/列表），先转义再渲染，避免 XSS */
function formatText(text) {
  let html = escapeHtml(text);
  html = html.replace(/`([^`\n]+)`/g, "<code>$1</code>");
  html = html.replace(/\*\*([^*\n]+)\*\*/g, "<strong>$1</strong>");
  return html
    .split(/\n/)
    .map((line) => {
      const trimmed = line.trim();
      if (/^[-*]\s+/.test(trimmed)) {
        return `<div class="md-li">${trimmed.replace(/^[-*]\s+/, "")}</div>`;
      }
      if (/^#{1,6}\s+/.test(trimmed)) {
        return `<div class="md-h">${trimmed.replace(/^#{1,6}\s+/, "")}</div>`;
      }
      return trimmed;
    })
    .join("<br>");
}

export default {
  name: "AiAssistant",
  components: {
    ChatDotRound,
    Close,
    Delete,
    Plus,
    CircleCheck,
    MagicStick,
    User,
  },
  data() {
    return {
      visible: false,
      input: "",
      sending: false,
      needLogin: !storage.getItem("accessToken"),
      conversationId: storage.getItem(CONVERSATION_KEY) || "",
      messages: [],
      abortController: null,
      historyLoaded: false,
      quickPrompts: [
        "帮我推荐一款蓝牙耳机",
        "我的订单到哪了？",
        "购物车里有什么？",
        "查一下我的待付款订单",
      ],
    };
  },
  methods: {
    toggle() {
      this.visible = !this.visible;
      if (this.visible) {
        this.needLogin = !storage.getItem("accessToken");
        this.$nextTick(() => {
          this.scrollToBottom();
          this.loadHistoryOnce();
        });
      }
    },
    goLogin() {
      this.visible = false;
      this.$router.push({
        path: "/login",
        query: { rePath: this.$route.path },
      });
    },
    onEnter(event) {
      if (event.shiftKey) return; // Shift+Enter 换行
      event.preventDefault();
      this.send();
    },
    quickAsk(text) {
      this.input = text;
      this.send();
    },
    /** 刷新后恢复上下文：把 Redis 里的会话记忆读回来展示 */
    async loadHistoryOnce() {
      if (
        this.historyLoaded ||
        !this.conversationId ||
        !storage.getItem("accessToken")
      ) {
        return;
      }
      this.historyLoaded = true;
      try {
        const list = await getHistory(this.conversationId);
        this.messages = (list || [])
          .filter((item) => item.text && item.role !== "system")
          .map((item) => ({
            id: `${item.role}-${Math.random().toString(36).slice(2)}`,
            role: item.role === "user" ? "user" : "assistant",
            text: item.text,
            actionId: "",
            actionState: "",
            actionResult: "",
            streaming: false,
            failed: false,
          }));
        this.$nextTick(this.scrollToBottom);
      } catch {
        // 历史读不到不影响新对话，静默处理
      }
    },
    async ensureConversation() {
      if (this.conversationId) return this.conversationId;
      const conversationId = await createConversation();
      if (!conversationId) {
        throw new Error("会话创建失败，请稍后重试");
      }
      this.conversationId = conversationId;
      storage.setItem(CONVERSATION_KEY, conversationId);
      return conversationId;
    },
    async send() {
      const text = this.input.trim();
      if (!text || this.sending) return;

      if (!storage.getItem("accessToken")) {
        this.needLogin = true;
        return;
      }

      this.needLogin = false;
      this.input = "";
      this.messages.push({
        id: `user-${Date.now()}`,
        role: "user",
        text,
        streaming: false,
        failed: false,
      });
      this.messages.push({
        id: `ai-${Date.now()}`,
        role: "assistant",
        text: "",
        actionId: "",
        actionState: "",
        actionResult: "",
        streaming: true,
        failed: false,
      });
      // 取到 reactive 代理再改，流式增量才会触发渲染
      const reply = this.messages[this.messages.length - 1];
      this.sending = true;
      this.$nextTick(this.scrollToBottom);

      try {
        const conversationId = await this.ensureConversation();
        this.abortController = new AbortController();
        await streamChat({
          conversationId,
          message: text,
          signal: this.abortController.signal,
          onDelta: (delta) => {
            reply.text += delta;
            this.scrollToBottom();
          },
        });
        if (!reply.text.trim()) {
          reply.text = "抱歉，我没太理解，换个说法再问一次可以吗？";
        }
        const actionId = extractActionId(reply.text);
        if (actionId) {
          reply.actionId = actionId;
          reply.actionState = "pending";
        }
      } catch (error) {
        if (error.name === "AbortError") {
          if (!reply.text.trim()) reply.text = "（已停止本次回答）";
        } else {
          this.handleChatError(error, reply);
        }
      } finally {
        reply.streaming = false;
        this.sending = false;
        this.abortController = null;
        this.$nextTick(this.scrollToBottom);
      }
    },
    handleChatError(error, reply) {
      const message = error.message || "AI 服务暂时不可用，请稍后重试";
      if (error.status === 400 && message.indexOf("会话") >= 0) {
        // 会话归属不对（比如换了账号登录），清掉本地会话，让用户重新发一次
        this.conversationId = "";
        storage.removeItem(CONVERSATION_KEY);
        reply.failed = true;
        reply.text = "当前会话已失效，已为你重置。请重新发送一次。";
        return;
      }
      if (error.status === 403) {
        this.needLogin = true;
      }
      reply.failed = true;
      reply.text = message;
      Message.error(message);
    },
    stop() {
      if (this.abortController) {
        this.abortController.abort();
      }
    },
    async doConfirm(item) {
      if (item.actionState === "loading") return;
      item.actionState = "loading";
      try {
        const result = await confirmAction(item.actionId);
        item.actionState = "done";
        item.actionResult = result;
        Message.success(result);
      } catch (error) {
        item.actionState = "pending";
        Message.error(error.message || "操作失败，请重试");
      }
    },
    newConversation() {
      if (this.sending) this.stop();
      this.conversationId = "";
      storage.removeItem(CONVERSATION_KEY);
      this.messages = [];
      this.historyLoaded = false;
      this.input = "";
      Message.info("已开启新会话");
    },
    async clearAll() {
      if (this.sending) this.stop();
      const conversationId = this.conversationId;
      this.newConversation();
      if (!conversationId) return;
      try {
        await clearConversation(conversationId);
        Message.success("已清空历史记忆");
      } catch {
        // 清空失败不影响本地已重置的会话
      }
    },
    renderText(item) {
      let text = item.text || "";
      // actionId 用确认按钮呈现，正文里不用再露出来
      if (item.actionId) {
        text = text.replace(/actionId\s*[=:：]\s*[A-Za-z0-9_-]{6,40}/g, "");
      }
      return formatText(text);
    },
    scrollToBottom() {
      const body = this.$refs.bodyEl;
      if (body) body.scrollTop = body.scrollHeight;
    },
  },
};
</script>

<style scoped lang="scss">
.ai-assistant {
  position: fixed;
  right: 60px;
  bottom: 60px;
  z-index: 2999;
}

/* 悬浮球 */
.ai-bubble {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  padding: 0 18px;
  color: #fff;
  cursor: pointer;
  background: linear-gradient(135deg, $theme_color, #ff7a3d);
  border-radius: 24px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.18);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 22px rgba(0, 0, 0, 0.22);
  }

  &.is-open {
    width: 48px;
    padding: 0;
    border-radius: 50%;
  }
}

.ai-bubble-text {
  margin-left: 8px;
  font-size: 14px;
  letter-spacing: 1px;
}

/* 面板 */
.ai-panel {
  position: absolute;
  right: 0;
  bottom: 62px;
  display: flex;
  flex-direction: column;
  width: 400px;
  height: 620px;
  max-height: calc(100vh - 140px);
  overflow: hidden;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.18);
}

.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  color: #fff;
  background: linear-gradient(135deg, $theme_color, #ff7a3d);
}

.ai-header-title {
  display: flex;
  align-items: center;
  font-size: 15px;
  font-weight: 600;

  span {
    margin-left: 6px;
  }
}

.ai-header-tools {
  display: flex;
  align-items: center;

  .el-icon {
    margin-left: 12px;
    cursor: pointer;
    opacity: 0.9;

    &:hover {
      opacity: 1;
    }
  }
}

.ai-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: $light_background_color;
}

.ai-notice {
  padding: 24px 16px;
  color: $light_content_color;
  text-align: center;
  background: #fff;
  border-radius: 10px;

  p {
    margin: 0 0 12px;
    font-size: 13px;
    line-height: 1.7;
  }
}

.ai-welcome {
  padding: 24px 4px;
  text-align: center;
}

.ai-welcome-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  color: #fff;
  background: linear-gradient(135deg, $theme_color, #ff7a3d);
  border-radius: 50%;
}

.ai-welcome-title {
  margin: 12px 0 4px;
  font-size: 15px;
  font-weight: 600;
  color: $light_title_color;
}

.ai-welcome-desc {
  margin: 0 0 16px;
  font-size: 12px;
  color: $light_sub_color;
}

.ai-quick {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}

.ai-quick-item {
  padding: 6px 12px;
  margin: 0 6px 8px;
  font-size: 12px;
  color: $light_content_color;
  cursor: pointer;
  background: #fff;
  border: 1px solid $border_color;
  border-radius: 14px;

  &:hover {
    color: $theme_color;
    border-color: $theme_color;
  }
}

.ai-msg {
  display: flex;
  margin-bottom: 14px;
}

.ai-avatar {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: #fff;
  background: $theme_color;
  border-radius: 50%;
}

.ai-msg-main {
  max-width: 300px;
  margin-left: 8px;
}

.ai-bubble-text {
  padding: 9px 12px;
  font-size: 13px;
  line-height: 1.7;
  color: $light_content_color;
  word-break: break-word;
  background: #fff;
  border-radius: 4px 10px 10px 10px;

  &.is-error {
    color: $error_color;
    background: #fef0f0;
  }

  :deep(code) {
    padding: 1px 4px;
    background: rgba(0, 0, 0, 0.06);
    border-radius: 3px;
  }

  :deep(.md-li) {
    position: relative;
    padding-left: 10px;

    &::before {
      content: "•";
      position: absolute;
      left: 0;
    }
  }

  :deep(.md-h) {
    font-weight: 600;
  }
}

.ai-msg-user {
  flex-direction: row-reverse;

  .ai-avatar {
    background: #8c8c8c;
  }

  .ai-msg-main {
    margin: 0 8px 0 0;
  }

  .ai-bubble-text {
    color: #fff;
    background: $theme_color;
    border-radius: 10px 4px 10px 10px;
  }
}

.ai-typing {
  display: inline-flex;
  align-items: center;
  height: 16px;
  margin-left: 6px;
  vertical-align: middle;

  i {
    width: 4px;
    height: 4px;
    margin-right: 3px;
    background: $light_sub_color;
    border-radius: 50%;
    animation: ai-blink 1s infinite alternate;

    &:nth-child(2) {
      animation-delay: 0.2s;
    }

    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

@keyframes ai-blink {
  from {
    opacity: 0.2;
  }

  to {
    opacity: 1;
  }
}

.ai-action {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  padding: 10px 12px;
  margin-top: 6px;
  font-size: 12px;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 8px;

  .el-button {
    margin-top: 6px;
  }
}

.ai-action-tip {
  flex: 1;
  min-width: 150px;
  color: #ad6800;
}

.ai-action-icon {
  margin-right: 6px;
  color: $success_color;

  &.is-done {
    font-size: 15px;
  }
}

.ai-footer {
  padding: 10px 12px 12px;
  background: #fff;
  border-top: 1px solid $border_color;

  :deep(.el-textarea__inner) {
    padding: 8px 10px;
    font-size: 13px;
    box-shadow: none;
  }
}

.ai-footer-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.ai-footer-hint {
  font-size: 11px;
  color: $light_sub_color;
}

/* 面板出现动画 */
.ai-slide-enter-active,
.ai-slide-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.ai-slide-enter-from,
.ai-slide-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>

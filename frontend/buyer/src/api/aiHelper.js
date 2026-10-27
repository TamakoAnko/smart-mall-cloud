/**
 * AI 助手接口层
 *
 * 所有请求统一打到网关（BASE.API_*.gateway），由网关按 /ai/** 转发到 lili-AIHelper，
 * 前端不直连 lili-AIHelper（9001），这样鉴权、跨域、限流都收敛在网关一层。
 *
 * 注意：lili-AIHelper 的返回值是「裸 DTO」，不像 lili-service 那样包一层 Result，
 *      所以拿的是 conversationId / message 本身，不要再去取 .result。
 */
import storage from "@/plugins/storage";

/** 网关地址：开发/生产都在 public/config.js 里配 */
const gatewayUrl =
  process.env.NODE_ENV === "development"
    ? BASE.API_DEV.gateway
    : BASE.API_PROD.gateway;

const AI_BASE = `${String(gatewayUrl || "").replace(/\/+$/, "")}/ai`;

/** 网关和 lili-AIHelper 都认这个头名（网关配置 lili.gateway.token-header=AccessToken，HTTP 头不区分大小写） */
function authHeaders(extra = {}) {
  const headers = { ...extra };
  const accessToken = storage.getItem("accessToken");
  if (accessToken) {
    headers["accessToken"] = accessToken;
  }
  const uuid = storage.getItem("uuid");
  if (uuid) {
    headers["uuid"] = uuid;
  }
  return headers;
}

/** 统一解析后端错误体：{ "message": "xxx" } */
async function toError(response) {
  let message = "";
  try {
    const data = await response.json();
    message = data?.message || data?.msg || "";
  } catch {
    // 网关直接返回的 403 文本等情况，拿不到 JSON 就用兜底文案
  }
  if (!message) {
    message =
      response.status === 403
        ? "登录状态已失效，请重新登录"
        : `请求失败（HTTP ${response.status}）`;
  }
  return Object.assign(new Error(message), { status: response.status });
}

/**
 * 建会话：后端用 accessToken 换 memberId，返回 conversationId（形如 "1001:8f3b..."）
 * @returns {Promise<string>} conversationId
 */
export async function createConversation() {
  const response = await fetch(`${AI_BASE}/conversations`, {
    method: "POST",
    headers: authHeaders({ Accept: "application/json" }),
  });
  if (!response.ok) {
    throw await toError(response);
  }
  const data = await response.json();
  return data?.conversationId || "";
}

/**
 * 查询会话历史（含压缩后的摘要消息，主要用于刷新后恢复上下文）
 * @returns {Promise<Array<{role:string,text:string}>>}
 */
export async function getHistory(conversationId) {
  const response = await fetch(
    `${AI_BASE}/conversations/${encodeURIComponent(conversationId)}/messages`,
    { method: "GET", headers: authHeaders({ Accept: "application/json" }) }
  );
  if (!response.ok) {
    throw await toError(response);
  }
  return (await response.json()) || [];
}

/** 清空某个会话的记忆 */
export async function clearConversation(conversationId) {
  const response = await fetch(
    `${AI_BASE}/conversations/${encodeURIComponent(conversationId)}`,
    { method: "DELETE", headers: authHeaders() }
  );
  if (!response.ok) {
    throw await toError(response);
  }
}

/**
 * 确认执行 AI 生成的写操作（加入购物车等），一次性消费
 * @returns {Promise<string>} 执行结果文案
 */
export async function confirmAction(actionId) {
  const response = await fetch(
    `${AI_BASE}/actions/${encodeURIComponent(actionId)}/confirm`,
    { method: "POST", headers: authHeaders({ Accept: "application/json" }) }
  );
  if (!response.ok) {
    throw await toError(response);
  }
  const data = await response.json();
  return data?.message || "操作已完成";
}

/**
 * 从一段 SSE 原始文本里取 data 字段（多条 data 行按规范用 \n 拼接）
 */
function readSseData(rawEvent) {
  const dataLines = [];
  for (const line of rawEvent.split(/\r?\n/)) {
    if (line.startsWith("data:")) {
      dataLines.push(line.slice(5).replace(/^ /, ""));
    }
  }
  return dataLines.length ? dataLines.join("\n") : null;
}

/**
 * 流式对话（SSE）
 *
 * 后端可能两种返回形态，这里都兼容：
 *  1) 标准 SSE（`data:片段\n\n`）—— Spring WebFlux 编码 Flux<String> 的默认行为；
 *  2) 裸文本流 —— Spring MVC 直接把 Flux<String> 的元素依次写出去，没有 data: 前缀。
 *
 * @param {object}   options
 * @param {string}   options.conversationId
 * @param {string}   options.message
 * @param {Function} options.onDelta  每次拿到增量文本时回调
 * @param {AbortSignal} [options.signal]
 * @returns {Promise<string>} 完整回复
 */
export async function streamChat({ conversationId, message, onDelta, signal }) {
  const response = await fetch(`${AI_BASE}/chat`, {
    method: "POST",
    headers: authHeaders({
      "Content-Type": "application/json",
      Accept: "text/event-stream",
    }),
    body: JSON.stringify({ conversationId, message }),
    signal,
  });

  if (!response.ok) {
    throw await toError(response);
  }
  if (!response.body) {
    throw new Error("当前浏览器不支持流式响应");
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");
  let full = "";
  let rawAll = "";
  let buffer = "";
  /** null=还没判断出来；sse=标准 SSE；raw=裸文本流 */
  let mode = null;

  const emit = (text) => {
    if (!text) return;
    full += text;
    onDelta(text);
  };

  while (true) {
    const { value, done } = await reader.read();
    if (done) break;

    const chunk = decoder.decode(value, { stream: true });
    if (chunk === "") continue;
    rawAll += chunk;

    if (mode === null) {
      // 首块内容以 data:/event:/id:/retry:/: 开头就是标准 SSE，否则按裸文本流处理
      mode = /^\s*(data|event|id|retry|:)/.test(chunk) ? "sse" : "raw";
    }

    if (mode === "raw") {
      emit(chunk);
      continue;
    }

    buffer += chunk;
    let boundary = /\r?\n\r?\n/.exec(buffer);
    while (boundary) {
      const rawEvent = buffer.slice(0, boundary.index);
      buffer = buffer.slice(boundary.index + boundary[0].length);
      const data = readSseData(rawEvent);
      if (data !== null) emit(data);
      boundary = /\r?\n\r?\n/.exec(buffer);
    }
  }

  const tail = decoder.decode().concat("");
  rawAll += tail;
  if (mode === "raw") {
    emit(tail);
  } else {
    buffer += tail;
    if (buffer.trim()) {
      const data = readSseData(buffer);
      if (data !== null) emit(data);
    }
  }

  // 兜底：被误判成 SSE 的裸文本流（正文恰好以 data 开头）至少保证内容不丢
  if (mode !== "raw" && full === "" && rawAll.trim()) {
    emit(rawAll);
  }

  return full;
}

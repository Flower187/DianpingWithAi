(function () {
  "use strict";
  if (document.getElementById("dianping-agent")) return;

  const stylesheet = document.createElement("link");
  stylesheet.rel = "stylesheet";
  stylesheet.href = "/css/agent-widget.css?v=20261107-2";
  document.head.appendChild(stylesheet);

  const host = document.createElement("div");
  host.id = "dianping-agent";
  host.innerHTML = `
    <button class="agent-launcher" type="button" aria-expanded="false" aria-controls="agent-panel">AI 助手</button>
    <section id="agent-panel" class="agent-panel" role="dialog" aria-label="AI 助手" hidden>
      <header class="agent-header">
        <div><strong>AI 助手</strong><small>每次提问独立处理，请带上所需背景</small></div>
        <button class="agent-close" type="button" aria-label="收起聊天">×</button>
      </header>
      <div class="agent-messages" role="log" aria-live="polite"></div>
      <p class="agent-status" role="status">可以咨询生活问题或让助手协助完成任务。</p>
      <form class="agent-form">
        <label for="agent-question">你的问题</label>
        <textarea id="agent-question" rows="2" maxlength="500" placeholder="请输入问题（最多 500 字）" required></textarea>
        <div class="agent-actions"><button class="agent-stop" type="button" hidden>停止接收</button><button class="agent-send" type="submit">发送</button></div>
      </form>
    </section>`;
  document.body.appendChild(host);

  const launcher = host.querySelector(".agent-launcher");
  const panel = host.querySelector(".agent-panel");
  const question = host.querySelector("textarea");
  const messages = host.querySelector(".agent-messages");
  const status = host.querySelector(".agent-status");
  const send = host.querySelector(".agent-send");
  const stop = host.querySelector(".agent-stop");
  let controller = null;

  function togglePanel(open) {
    panel.hidden = !open;
    launcher.setAttribute("aria-expanded", String(open));
    if (open) question.focus();
    else launcher.focus();
  }
  launcher.addEventListener("click", () => togglePanel(panel.hidden));
  host.querySelector(".agent-close").addEventListener("click", () => togglePanel(false));
  host.addEventListener("keydown", event => {
    if (event.key === "Escape") togglePanel(false);
  });
  stop.addEventListener("click", () => controller?.abort());
  window.addEventListener("beforeunload", () => controller?.abort());

  function addMessage(text, fromUser) {
    const item = document.createElement("div");
    item.className = fromUser ? "agent-message agent-user" : "agent-message agent-answer";
    item.textContent = text;
    messages.appendChild(item);
    messages.scrollTop = messages.scrollHeight;
    return item;
  }

  host.querySelector("form").addEventListener("submit", async event => {
    event.preventDefault();
    const text = question.value.trim();
    if (!text || controller) return;
    addMessage(text, true);
    const answer = addMessage("", false);
    question.value = "";
    send.disabled = true;
    stop.hidden = false;
    status.textContent = "助手正在处理，请稍候…";
    controller = new AbortController();
    let reader;
    try {
      const response = await fetch("/api/ai/manus/chat?message=" + encodeURIComponent(text), {
        headers: { Accept: "text/event-stream" },
        signal: controller.signal
      });
      if (!response.ok) throw new Error("服务返回 HTTP " + response.status);
      if (!response.headers.get("content-type")?.includes("text/event-stream")) {
        throw new Error("服务没有返回聊天数据");
      }
      if (!response.body) throw new Error("浏览器不支持流式响应");
      reader = response.body.getReader();
      const decoder = new TextDecoder();
      let lineBuffer = "";
      let dataLines = [];

      function readLines() {
        let newline;
        while ((newline = lineBuffer.indexOf("\n")) !== -1) {
          const line = lineBuffer.slice(0, newline).replace(/\r$/, "");
          lineBuffer = lineBuffer.slice(newline + 1);
          if (line === "") {
            if (dataLines.length) {
              // SseEmitter 的多行 data 合并为一个完整事件。
              answer.textContent += (answer.textContent ? "\n\n" : "") + dataLines.join("\n");
              messages.scrollTop = messages.scrollHeight;
              dataLines = [];
            }
          } else if (line.startsWith("data:")) {
            dataLines.push(line.slice(5).replace(/^ /, ""));
          }
        }
      }

      while (true) {
        const chunk = await reader.read();
        lineBuffer += decoder.decode(chunk.value, { stream: !chunk.done });
        readLines();
        if (chunk.done) break;
      }
      // 处理服务在最后一个事件后直接关闭连接的情况。
      lineBuffer += "\n\n";
      readLines();
      if (!answer.textContent) throw new Error("助手未返回内容，请重试");
      status.textContent = "回复已结束，可以继续提问。";
    } catch (error) {
      const message = error.name === "AbortError"
        ? "已停止接收，后台任务可能仍在执行。"
        : "连接未完成，请检查 AI 服务后重试。" + error.message;
      status.textContent = message;
      if (!answer.textContent) answer.textContent = message;
    } finally {
      reader?.releaseLock();
      controller = null;
      send.disabled = false;
      stop.hidden = true;
    }
  });
})();

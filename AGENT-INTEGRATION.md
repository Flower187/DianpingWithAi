# 点评 AI 助手接入

`frontend/` 保存了从现有 Nginx `html/hmdp` 复制的点评前端。
所有页面直接加载带版本号的 `js/agent-widget.js`，避免浏览器缓存旧版公共脚本导致入口缺失。

## 访问链路

- 点评页面由 Nginx 8080 提供。
- 原有 `/api` 请求去除前缀后转到点评后端 8081。
- `/api/ai/manus/chat` 保留完整路径转到 AI-agent 8123。该路由关闭代理缓冲，读取超时为 360 秒。

两个 Java 服务独立运行。点评保持 Java 8 / Boot 2，AI-agent 保持 Java 21 / Boot 3。
AI 功能复用现有 `AiController` 和 `FitnessManus`。无需把 AI-agent 的 jar 加入点评依赖。

## 安装到现有 Nginx

在本项目根目录运行以下 PowerShell 命令。脚本安装入口脚本、样式、页面和代理配置，备份原文件，并检查配置及重载或启动 Nginx。需要有 Nginx 目录写入权限。

```powershell
.\deploy\install-agent.ps1
```

默认 Nginx 目录是 `F:\develop\nginx\hmdp\nginx-1.18.0\nginx-1.18.0`，可以通过 `-NginxRoot` 参数指定。
`deploy/nginx.conf` 基于这份现有配置生成。如果部署前改过原配置，请先合并差异再安装。

启动点评的 `HmDianPingApplication`（8081，依赖 MySQL、Redis）和 AI-agent 的 `AiAgentMeApplication`（8123，使用其现有模型及工具配置），打开 `http://localhost:8080/` 并刷新。
右下角点击「AI 助手」，输入问题后发送。

## 当前能力边界

- 每次请求新建 Agent，聊天面板保留当前页面显示的消息，后端不会自动继承前一次对话上下文。
- 「停止接收」终止浏览器接收，后台工具任务可能继续执行。
- 复用 Agent 现有工具能力。尚未加入点评专属查店、优惠券查询工具，也尚未加入点评登录身份校验，部署范围应保持为本地开发演示。
- 前端以纯文本显示 SSE 事件，使用正常 EOF 判断结束。后端未启动时显示连接错误，可恢复后重新发送。

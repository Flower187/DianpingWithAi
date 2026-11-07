# DianpingWithAi
探店app+ai助手问答


## 本地运行

- `hm-dianping/` 为点评后端，使用 Java 8，默认端口 8081，依赖 MySQL 和 Redis。
- `ai-agent/` 为独立 Agent 后端，使用 Java 21，默认端口 8123。
- `frontend/` 为点评前端，右下角提供 AI 助手入口。
- `deploy/` 保存 Nginx 配置和带备份的本机安装脚本。默认端口 8080。

### 配置凭据

点评支持 `DB_PASSWORD` 和 `REDIS_PASSWORD` 环境变量。本机私密配置保存在不提交的 `application-sensitive.yml` 中。

运行 Agent 前，将 `ai-agent/src/main/resources/application-sensitive.example.yml` 复制为同目录 `application-sensitive.yml`，填写或设置示例中的环境变量。模型需要有效的 DashScope 额度。

进入 `ai-agent/image-search-mcp-server` 执行 `mvn -DskipTests package`，再从 `ai-agent` 目录运行主服务。跨机器部署时可用 `JAVA_COMMAND`、`IMAGE_MCP_JAR` 覆盖 MCP 进程配置。

两个后端分别执行 `mvn spring-boot:run`。仅检查 Agent 接口连通性时可添加 `-Dspring-boot.run.arguments=--spring.main.lazy-initialization=true`，实际模型调用仍需要有效额度。

按照 [接入说明](AGENT-INTEGRATION.md) 安装前端到 Nginx 后，访问 `http://localhost:8080/`。

当前每次提问独立处理，尚未接入点评用户身份与查店工具，适用于本地演示。

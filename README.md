# Spring AI Weather Agent

一个使用 Java 21、Spring Boot、Spring AI 和和风天气 API 实现的完整工具调用型天气 Agent。

它不只是一个天气接口封装：`WeatherAgent` 将大模型、会话记忆和 `get_current_weather`
工具组合起来，由 Spring AI 执行以下 Tool Calling/ReAct 循环：

```text
用户问题 -> 模型判断 -> 工具调用 -> 和风天气 -> Observation -> 模型最终回答
```

## 技术栈

- Java 21
- Spring Boot 3.5
- Spring AI 1.1
- Spring MVC `RestClient`
- DeepSeek `deepseek-chat`
- Resilience4j Retry + Circuit Breaker
- 和风天气 GeoAPI + 实时天气 API

## 核心模块

```text
web/WeatherAgentController  HTTP 入口
agent/WeatherAgent          Agent 入口和会话隔离
config/AgentConfiguration   Prompt、Memory、Tool 注册
tool/WeatherTools           模型可调用的天气工具
service/WeatherService      领域编排与结构化降级
qweather/QWeatherApiClient  外部 API 客户端
```

`ChatClient` 的工具调用循环会持续执行“模型 -> 工具 -> 模型”，直到模型返回不含工具调用的
最终回答。对话通过 `sessionId` 隔离，并保留最近 20 条消息。

## 配置

请先在和风天气控制台创建项目，获取专属 API Host 和 API Key。API Host 请使用控制台提供的
`*.qweatherapi.com` 域名，不要使用旧公共域名。

推荐通过环境变量配置：

```bash
export QWEATHER_API_HOST="https://你的专属域名.qweatherapi.com"
export QWEATHER_API_KEY="你的和风天气APIKey"

export DEEPSEEK_API_KEY="你的DeepSeek API Key"
export DEEPSEEK_BASE_URL="https://api.deepseek.com"
export DEEPSEEK_MODEL="deepseek-chat"
```

也可以在项目根目录创建不会提交到 Git、也不会打包进 Jar 的 `application-local.yml`。
项目会自动加载它。不要把真实密钥提交到仓库。

默认使用 DeepSeek 的 `deepseek-chat`，它支持本项目需要的工具调用。模型层由 Spring AI
`spring-ai-starter-model-deepseek` 接入，Agent 与天气工具不依赖 DeepSeek 的具体 HTTP API。

## 启动与调用

```bash
mvn spring-boot:run
```

```bash
curl --get http://localhost:8080/api/agent/chat \
  --data-urlencode 'sessionId=demo-user' \
  --data-urlencode 'message=杭州现在天气怎么样，需要带伞吗？'
```

响应示例：

```json
{
  "sessionId": "demo-user",
  "answer": "杭州当前……"
}
```

## 容错策略

- 连接超时 2 秒，读取超时 3 秒。
- 网络异常和服务端 5xx 最多请求 2 次；参数错误和 4xx 不重试。
- 连续失败达到阈值后熔断 30 秒。
- 工具将失败转换为包含 `errorCode` 的 `WeatherResult`，让 Agent 如实说明失败，禁止编造。
- Agent 未配置和风天气密钥时仍可启动，工具会返回 `QWEATHER_NOT_CONFIGURED`。
- GeoAPI 结果不落地缓存，以遵守和风天气关于地理信息数据缓存的许可限制。

## 测试

```bash
mvn test
```

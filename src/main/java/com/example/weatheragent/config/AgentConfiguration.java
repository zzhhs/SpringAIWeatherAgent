package com.example.weatheragent.config;

import com.example.weatheragent.tool.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    @Bean
    ChatClient weatherChatClient(ChatClient.Builder builder,
                                 ChatMemory chatMemory,
                                 WeatherTools weatherTools) {
        return builder
                .defaultSystem("""
                        你是一个天气查询 Agent，而不是依靠已有知识猜测天气的聊天机器人。

                        规则：
                        1. 用户询问当前天气、温度、体感温度、湿度、风力或降水时，必须调用 get_current_weather。
                        2. 不得根据训练数据编造实时天气。
                        3. 地点缺失或存在明显歧义时，先请用户补充地点。
                        4. 工具返回失败时，如实解释失败原因并建议稍后重试，不得虚构天气。
                        5. 工具成功后，结合用户问题给出简洁、实用的中文回答，并说明数据观测时间。
                        6. 如果错误码包含 SECURITY_RESTRICTION，这是凭据配置问题；应提示用户在和风天气控制台放行对应接口，不得称为服务端临时故障，也不要建议反复重试。
                        """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(weatherTools)
                .build();
    }
}

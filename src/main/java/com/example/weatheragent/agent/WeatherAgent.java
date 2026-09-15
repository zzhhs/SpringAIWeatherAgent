package com.example.weatheragent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class WeatherAgent {

    private final ChatClient chatClient;

    public WeatherAgent(ChatClient weatherChatClient) {
        this.chatClient = weatherChatClient;
    }

    /**
     * Runs one turn of the weather agent. ChatClient's tool-calling loop performs
     * model -> tool -> observation -> model iterations until a final answer is produced.
     */
    public String chat(String sessionId, String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }
}

package com.example.weatheragent.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.weatheragent.agent.WeatherAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WeatherAgentController.class)
class WeatherAgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherAgent weatherAgent;

    @Test
    void acceptsChatParametersFromGetQueryString() throws Exception {
        when(weatherAgent.chat("user-001", "杭州天气")).thenReturn("杭州当前多云");

        mockMvc.perform(get("/api/agent/chat")
                        .param("sessionId", "user-001")
                        .param("message", "杭州天气"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("user-001"))
                .andExpect(jsonPath("$.answer").value("杭州当前多云"));
    }

    @Test
    void rejectsTheOldPostEndpoint() throws Exception {
        mockMvc.perform(post("/api/agent/chat"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void validatesRequiredQueryParameters() throws Exception {
        mockMvc.perform(get("/api/agent/chat")
                        .param("sessionId", "user-001"))
                .andExpect(status().isBadRequest());
    }
}

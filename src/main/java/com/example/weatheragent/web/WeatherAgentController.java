package com.example.weatheragent.web;

import com.example.weatheragent.agent.WeatherAgent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/agent")
public class WeatherAgentController {

    private final WeatherAgent weatherAgent;

    public WeatherAgentController(WeatherAgent weatherAgent) {
        this.weatherAgent = weatherAgent;
    }

    @GetMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestParam
            @NotBlank(message = "sessionId 不能为空")
            @Size(max = 100, message = "sessionId 不能超过 100 个字符")
            String sessionId,
            @RequestParam
            @NotBlank(message = "message 不能为空")
            @Size(max = 2000, message = "message 不能超过 2000 个字符")
            String message) {
        String answer = weatherAgent.chat(sessionId, message);
        return ResponseEntity.ok(new ChatResponse(sessionId, answer));
    }
}

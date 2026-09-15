package com.example.weatheragent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.ai.deepseek.api-key=test-model-key",
        "qweather.api-host=https://test.qweatherapi.com",
        "qweather.api-key=test-weather-key"
})
class WeatherAgentApplicationTest {

    @Test
    void applicationContextLoadsWithAgentAndToolConfiguration() {
    }
}

package com.example.weatheragent.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import com.example.weatheragent.domain.WeatherResult;
import com.example.weatheragent.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.annotation.Tool;

class WeatherToolsTest {

    @Test
    void exposesCurrentWeatherAsAnAiTool() throws Exception {
        WeatherService service = org.mockito.Mockito.mock(WeatherService.class);
        WeatherResult expected = WeatherResult.success(
                "北京", "晴", 25.0, 24.0, 40, "北风", 2, "2026-09-15T23:00+08:00");
        when(service.getCurrentWeather("北京")).thenReturn(expected);

        WeatherTools tools = new WeatherTools(service);
        WeatherResult actual = tools.getCurrentWeather("北京");

        Method method = WeatherTools.class.getMethod("getCurrentWeather", String.class);
        Tool annotation = method.getAnnotation(Tool.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).isEqualTo("get_current_weather");
        assertThat(actual).isEqualTo(expected);
        verify(service).getCurrentWeather("北京");
    }
}

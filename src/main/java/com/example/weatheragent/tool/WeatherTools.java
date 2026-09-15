package com.example.weatheragent.tool;

import com.example.weatheragent.domain.WeatherResult;
import com.example.weatheragent.service.WeatherService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTools {

    private final WeatherService weatherService;

    public WeatherTools(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Tool(name = "get_current_weather", description = """
            查询指定城市的实时天气，包括天气现象、温度、体感温度、湿度和风力。
            当用户询问现在或当前天气时使用。此工具不提供历史天气或未来天气预报。
            """)
    public WeatherResult getCurrentWeather(
            @ToolParam(description = "城市、区县或地点名称，例如：北京、杭州市、西湖区")
            String location) {
        return weatherService.getCurrentWeather(location);
    }
}

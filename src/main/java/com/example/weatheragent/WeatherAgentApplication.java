package com.example.weatheragent;

import com.example.weatheragent.config.QWeatherProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(QWeatherProperties.class)
public class WeatherAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherAgentApplication.class, args);
    }
}

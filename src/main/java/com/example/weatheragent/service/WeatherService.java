package com.example.weatheragent.service;

import java.time.OffsetDateTime;

import com.example.weatheragent.config.QWeatherProperties;
import com.example.weatheragent.domain.WeatherResult;
import com.example.weatheragent.qweather.QWeatherApiClient;
import com.example.weatheragent.qweather.QWeatherApiException;
import com.example.weatheragent.qweather.QWeatherDtos;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final QWeatherProperties properties;
    private final QWeatherApiClient apiClient;

    public WeatherService(QWeatherProperties properties, QWeatherApiClient apiClient) {
        this.properties = properties;
        this.apiClient = apiClient;
    }

    public WeatherResult getCurrentWeather(String location) {
        String normalizedLocation = location == null ? "" : location.trim();
        if (normalizedLocation.isBlank()) {
            return WeatherResult.failure(null, "INVALID_LOCATION", "地点不能为空");
        }
        if (!properties.isConfigured()) {
            return WeatherResult.failure(normalizedLocation, "QWEATHER_NOT_CONFIGURED",
                    "天气服务尚未配置 API Host 或 API Key");
        }

        try {
            QWeatherDtos.Location resolved = apiClient.lookupLocation(normalizedLocation);
            QWeatherDtos.WeatherResponse response =
                    apiClient.getCurrentWeather(resolved.lat(), resolved.lon());
            QWeatherDtos.Wind wind = response.wind();

            return WeatherResult.success(
                    resolved.displayName(),
                    response.condition().text(),
                    response.temperature().value(),
                    response.feelsLike() == null ? null : response.feelsLike().value(),
                    percentage(response.humidity()),
                    wind == null || wind.direction() == null
                            ? null : compassName(wind.direction().compass()),
                    wind == null ? null : wind.scale(),
                    OffsetDateTime.now().toString());
        }
        catch (CallNotPermittedException ex) {
            log.warn("QWeather circuit breaker is open for location={}", normalizedLocation);
            return WeatherResult.failure(normalizedLocation, "QWEATHER_CIRCUIT_OPEN",
                    "天气服务连续失败，已临时熔断，请稍后重试");
        }
        catch (QWeatherApiException ex) {
            if (ex.getCause() == null) {
                log.warn("QWeather request failed for location={}, code={}",
                        normalizedLocation, ex.getCode());
            }
            else {
                log.warn("QWeather request failed for location={}, code={}",
                        normalizedLocation, ex.getCode(), ex);
            }
            return WeatherResult.failure(normalizedLocation, ex.getCode(), ex.getMessage());
        }
        catch (RuntimeException ex) {
            log.error("Unexpected weather tool failure for location={}", normalizedLocation, ex);
            return WeatherResult.failure(normalizedLocation, "WEATHER_TOOL_ERROR",
                    "天气查询暂时失败，请稍后重试");
        }
    }

    private static Integer percentage(Double value) {
        return value == null ? null : (int) Math.round(value * 100);
    }

    private static String compassName(String compass) {
        if (compass == null) {
            return null;
        }
        return switch (compass.toLowerCase()) {
            case "n" -> "北风";
            case "nne" -> "北东北风";
            case "ne" -> "东北风";
            case "ene" -> "东东北风";
            case "e" -> "东风";
            case "ese" -> "东东南风";
            case "se" -> "东南风";
            case "sse" -> "南东南风";
            case "s" -> "南风";
            case "ssw" -> "南西南风";
            case "sw" -> "西南风";
            case "wsw" -> "西西南风";
            case "w" -> "西风";
            case "wnw" -> "西西北风";
            case "nw" -> "西北风";
            case "nnw" -> "北西北风";
            case "vrb" -> "风向不定";
            default -> compass;
        };
    }
}

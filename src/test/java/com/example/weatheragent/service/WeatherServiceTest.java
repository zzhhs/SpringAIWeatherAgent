package com.example.weatheragent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;

import com.example.weatheragent.config.QWeatherProperties;
import com.example.weatheragent.domain.WeatherResult;
import com.example.weatheragent.qweather.QWeatherApiClient;
import com.example.weatheragent.qweather.QWeatherApiException;
import com.example.weatheragent.qweather.QWeatherDtos.Condition;
import com.example.weatheragent.qweather.QWeatherDtos.Direction;
import com.example.weatheragent.qweather.QWeatherDtos.Location;
import com.example.weatheragent.qweather.QWeatherDtos.Measurement;
import com.example.weatheragent.qweather.QWeatherDtos.Wind;
import com.example.weatheragent.qweather.QWeatherDtos.WeatherResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private QWeatherApiClient apiClient;

    @Test
    void mapsQWeatherResponseToToolResult() {
        WeatherService service = serviceWithValidConfiguration();
        Location hangzhou = new Location("杭州", "101210101", "30.27415", "120.15515",
                "浙江省", "杭州市", "中国");
        WeatherResponse response = new WeatherResponse(
                new Condition("多云", "101"),
                new Measurement(24.0, "°C"),
                new Measurement(25.0, "°C"),
                0.67,
                new Wind(new Direction(90, "e"), new Measurement(2.0, "m/s"), 2));

        when(apiClient.lookupLocation("杭州")).thenReturn(hangzhou);
        when(apiClient.getCurrentWeather("30.27415", "120.15515")).thenReturn(response);

        WeatherResult result = service.getCurrentWeather(" 杭州 ");

        assertThat(result.success()).isTrue();
        assertThat(result.location()).isEqualTo("杭州，杭州市");
        assertThat(result.weather()).isEqualTo("多云");
        assertThat(result.temperatureCelsius()).isEqualTo(24.0);
        assertThat(result.humidityPercent()).isEqualTo(67);
        assertThat(result.windDirection()).isEqualTo("东风");
        assertThat(result.observationTime()).isNotBlank();
    }

    @Test
    void returnsStructuredFailureWhenQWeatherIsNotConfigured() {
        QWeatherProperties properties = new QWeatherProperties(
                "https://replace-me.qweatherapi.com", "replace-me",
                Duration.ofSeconds(2), Duration.ofSeconds(3));
        WeatherService service = new WeatherService(properties, apiClient);

        WeatherResult result = service.getCurrentWeather("北京");

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("QWEATHER_NOT_CONFIGURED");
        verifyNoInteractions(apiClient);
    }

    @Test
    void convertsUpstreamExceptionToSafeToolFailure() {
        WeatherService service = serviceWithValidConfiguration();
        when(apiClient.lookupLocation("不存在的城市"))
                .thenThrow(new QWeatherApiException("LOCATION_NOT_FOUND", "未找到指定城市"));

        WeatherResult result = service.getCurrentWeather("不存在的城市");

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("LOCATION_NOT_FOUND");
        assertThat(result.errorMessage()).isEqualTo("未找到指定城市");
    }

    private WeatherService serviceWithValidConfiguration() {
        QWeatherProperties properties = new QWeatherProperties(
                "https://example.qweatherapi.com", "test-key",
                Duration.ofSeconds(2), Duration.ofSeconds(3));
        return new WeatherService(properties, apiClient);
    }
}

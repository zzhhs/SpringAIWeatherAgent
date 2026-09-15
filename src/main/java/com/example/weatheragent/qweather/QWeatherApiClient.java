package com.example.weatheragent.qweather;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.weatheragent.qweather.QWeatherDtos.GeoResponse;
import com.example.weatheragent.qweather.QWeatherDtos.Location;
import com.example.weatheragent.qweather.QWeatherDtos.WeatherResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class QWeatherApiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public QWeatherApiClient(@Qualifier("qWeatherRestClient") RestClient restClient,
                             ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @Retry(name = "qweather")
    @CircuitBreaker(name = "qweather")
    public Location lookupLocation(String location) {
        try {
            GeoResponse response = restClient.get()
                    .uri(uri -> uri.path("/geo/v2/city/lookup")
                            .queryParam("location", location)
                            .queryParam("number", 1)
                            .queryParam("lang", "zh")
                            .build())
                    .retrieve()
                    .body(GeoResponse.class);

            if (response == null || !"200".equals(response.code())) {
                throw new QWeatherApiException("QWEATHER_GEO_ERROR",
                        "地理位置查询失败，服务代码：" + (response == null ? "EMPTY" : response.code()));
            }
            if (response.location() == null || response.location().isEmpty()) {
                throw new QWeatherApiException("LOCATION_NOT_FOUND", "未找到指定城市");
            }
            return response.location().getFirst();
        }
        catch (QWeatherApiException ex) {
            throw ex;
        }
        catch (RestClientResponseException ex) {
            throw httpException("地理位置服务", ex.getStatusCode(), ex.getResponseBodyAsString());
        }
        catch (RestClientException ex) {
            throw new QWeatherTransientException("QWEATHER_UNAVAILABLE", "地理位置服务暂时不可用", ex);
        }
    }

    @Retry(name = "qweather")
    @CircuitBreaker(name = "qweather")
    public WeatherResponse getCurrentWeather(String latitude, String longitude) {
        try {
            String normalizedLatitude = coordinate(latitude, "纬度");
            String normalizedLongitude = coordinate(longitude, "经度");
            WeatherResponse response = restClient.get()
                    .uri(uri -> uri.path("/weather/v1/current/{latitude}/{longitude}")
                            .queryParam("lang", "zh")
                            .queryParam("localTime", true)
                            .build(normalizedLatitude, normalizedLongitude))
                    .retrieve()
                    .body(WeatherResponse.class);

            if (response == null || response.condition() == null
                    || response.temperature() == null) {
                throw new QWeatherApiException("QWEATHER_WEATHER_ERROR",
                        "实时天气服务返回的数据不完整");
            }
            return response;
        }
        catch (QWeatherApiException ex) {
            throw ex;
        }
        catch (RestClientResponseException ex) {
            throw httpException("实时天气服务", ex.getStatusCode(), ex.getResponseBodyAsString());
        }
        catch (RestClientException ex) {
            throw new QWeatherTransientException("QWEATHER_UNAVAILABLE", "实时天气服务暂时不可用", ex);
        }
    }

    private static String coordinate(String value, String name) {
        try {
            return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
        }
        catch (RuntimeException ex) {
            throw new QWeatherApiException("INVALID_COORDINATE", name + "格式不正确", ex);
        }
    }

    private QWeatherApiException httpException(String service, HttpStatusCode status, String responseBody) {
        String errorCode = "QWEATHER_HTTP_ERROR";
        String errorTitle = "";
        try {
            JsonNode error = objectMapper.readTree(responseBody).path("error");
            errorTitle = error.path("title").asText("");
            String type = error.path("type").asText("");
            if (!type.isBlank()) {
                int fragmentIndex = type.lastIndexOf('#');
                String typeName = fragmentIndex >= 0
                        ? type.substring(fragmentIndex + 1)
                        : type.substring(type.lastIndexOf('/') + 1);
                String slug = typeName
                        .replaceAll("[^A-Za-z0-9]+", "_")
                        .toUpperCase();
                if (!slug.isBlank()) {
                    errorCode = "QWEATHER_" + slug;
                }
            }
        }
        catch (Exception ignored) {
            // Some gateways return an empty or non-JSON error body.
        }

        String message = service + "返回 HTTP " + status.value();
        if (!errorTitle.isBlank()) {
            message += "（" + errorTitle + "）";
        }
        if ("QWEATHER_SECURITY_RESTRICTION".equals(errorCode)) {
            message += "，当前 API Key 的安全限制未允许该接口，请在和风天气控制台放行 GeoAPI 城市搜索";
        }
        return status.is5xxServerError()
                ? new QWeatherTransientException(errorCode, message)
                : new QWeatherApiException(errorCode, message);
    }
}

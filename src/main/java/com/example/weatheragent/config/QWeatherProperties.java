package com.example.weatheragent.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "qweather")
public record QWeatherProperties(
        String apiHost,
        String apiKey,
        Duration connectTimeout,
        Duration readTimeout) {

    public QWeatherProperties {
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(2) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(3) : readTimeout;
    }

    public boolean isConfigured() {
        return hasText(apiHost) && hasText(apiKey)
                && !apiHost.contains("replace-me") && !apiKey.contains("replace-me");
    }

    public String normalizedBaseUrl() {
        if (!hasText(apiHost)) {
            return "https://replace-me.qweatherapi.com";
        }
        return apiHost.startsWith("http://") || apiHost.startsWith("https://")
                ? apiHost
                : "https://" + apiHost;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

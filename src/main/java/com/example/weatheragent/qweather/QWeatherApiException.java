package com.example.weatheragent.qweather;

public class QWeatherApiException extends RuntimeException {

    private final String code;

    public QWeatherApiException(String code, String message) {
        super(message);
        this.code = code;
    }

    public QWeatherApiException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

package com.example.weatheragent.qweather;

public class QWeatherTransientException extends QWeatherApiException {

    public QWeatherTransientException(String code, String message) {
        super(code, message);
    }

    public QWeatherTransientException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}

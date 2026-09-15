package com.example.weatheragent.domain;

public record WeatherResult(
        boolean success,
        String location,
        String weather,
        Double temperatureCelsius,
        Double feelsLikeCelsius,
        Integer humidityPercent,
        String windDirection,
        Integer windScale,
        String observationTime,
        String errorCode,
        String errorMessage) {

    public static WeatherResult success(String location, String weather,
                                        Double temperatureCelsius, Double feelsLikeCelsius,
                                        Integer humidityPercent, String windDirection,
                                        Integer windScale, String observationTime) {
        return new WeatherResult(true, location, weather, temperatureCelsius,
                feelsLikeCelsius, humidityPercent, windDirection, windScale,
                observationTime, null, null);
    }

    public static WeatherResult failure(String location, String errorCode, String errorMessage) {
        return new WeatherResult(false, location, null, null, null,
                null, null, null, null, errorCode, errorMessage);
    }
}

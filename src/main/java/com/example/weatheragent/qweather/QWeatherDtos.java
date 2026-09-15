package com.example.weatheragent.qweather;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public final class QWeatherDtos {

    private QWeatherDtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeoResponse(String code, List<Location> location) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(String name, String id, String lat, String lon,
                           String adm1, String adm2, String country) {
        public String displayName() {
            String region = adm2 == null || adm2.isBlank() || adm2.equals(name) ? adm1 : adm2;
            return region == null || region.isBlank() || region.equals(name)
                    ? name
                    : name + "，" + region;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherResponse(Condition condition, Measurement temperature,
                                  Measurement feelsLike, Double humidity, Wind wind) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Condition(String text, String code) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Measurement(Double value, String unit) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wind(Direction direction, Measurement speed, Integer scale) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Direction(Integer degree, String compass) {
    }
}

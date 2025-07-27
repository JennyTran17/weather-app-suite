package com.proj.weather_consumer.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherEvent {
    private String city;
    private String condition;
    private double temperatureCelsius;
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeStamp;

    @Override
    public String toString() {
        return "WeatherEvent{" +
                "city='" + city + '\'' +
                ", condition='" + condition + '\'' +
                ", temperatureCelsius=" + temperatureCelsius +
                ", message='" + message + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
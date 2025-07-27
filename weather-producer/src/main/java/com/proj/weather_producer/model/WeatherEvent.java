package com.proj.weather_producer.model;

import java.time.LocalDateTime;

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
    private LocalDateTime timeStamp;


}

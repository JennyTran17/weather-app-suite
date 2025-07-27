package com.proj.weather_consumer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "weather_notifications")
public class WeatherNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String city;
    
    private String condition;
    
    private double temperatureCelsius;
    
    @Column(length = 1000)
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeStamp;
    
    // Constructor to create from WeatherEvent
    public WeatherNotification(WeatherEvent event) {
        this.city = event.getCity();
        this.condition = event.getCondition();
        this.temperatureCelsius = event.getTemperatureCelsius();
        this.message = event.getMessage();
        this.timeStamp = event.getTimeStamp();
    }
}
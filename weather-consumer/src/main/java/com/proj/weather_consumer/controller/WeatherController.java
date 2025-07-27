package com.proj.weather_consumer.controller;

import com.proj.weather_consumer.model.WeatherEvent;
import com.proj.weather_consumer.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for manual weather notification testing
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final NotificationService notificationService;

    @Autowired
    public WeatherController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Endpoint to manually send a test weather notification
     */
    @PostMapping("/test-notification")
    public ResponseEntity<Map<String, String>> sendTestNotification(@RequestBody WeatherEvent weatherEvent) {
        // Set timestamp if not provided
        if (weatherEvent.getTimeStamp() == null) {
            weatherEvent.setTimeStamp(LocalDateTime.now());
        }
        
        // Send the notification
        notificationService.sendWeatherNotification(weatherEvent);
        
        // Return success response
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Test notification sent successfully");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint to get a sample weather event structure
     */
    @GetMapping("/sample")
    public ResponseEntity<WeatherEvent> getSampleWeatherEvent() {
        WeatherEvent sample = new WeatherEvent(
            "Sample City",
            "Clear",
            25.5,
            "Clear skies with mild temperature",
            LocalDateTime.now()
        );
        return ResponseEntity.ok(sample);
    }
}
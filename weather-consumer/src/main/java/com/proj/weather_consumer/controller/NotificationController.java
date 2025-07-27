package com.proj.weather_consumer.controller;

import com.proj.weather_consumer.model.WeatherNotification;
import com.proj.weather_consumer.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<WeatherNotification> getRecentNotifications() {
        return notificationService.getRecentNotifications();
    }
}
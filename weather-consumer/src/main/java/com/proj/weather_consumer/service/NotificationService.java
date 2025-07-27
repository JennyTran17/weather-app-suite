package com.proj.weather_consumer.service;
import com.proj.weather_consumer.model.WeatherEvent;
import com.proj.weather_consumer.model.WeatherNotification;
import com.proj.weather_consumer.repository.WeatherNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.proj.weather_consumer.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final WeatherNotificationRepository notificationRepository;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate, 
                              WeatherNotificationRepository notificationRepository) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepository = notificationRepository;
    }

    public void sendWeatherNotification(WeatherEvent notification) {
        // Save notification to database
        WeatherNotification persistentNotification = new WeatherNotification(notification);
        notificationRepository.save(persistentNotification);
        
        // Send a message to all subscribers of the "/topic/weather-notifications" destination
        messagingTemplate.convertAndSend("/topic/weather-notifications", notification);
        LOGGER.info("Sent weather notification to UI and saved to database: {} ", notification);
    }

    // You might also want to send to a specific user
    public void sendNotificationToUser(String userId, WeatherEvent notification) {
        // Save notification to database
        WeatherNotification persistentNotification = new WeatherNotification(notification);
        notificationRepository.save(persistentNotification);
        
        // This sends to a user-specific queue, e.g., /user/{userId}/queue/notifications
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
        LOGGER.info("Sent user-specific notification to UI for user {} and saved to database: {} ", userId, notification);
    }
    
    public List<WeatherNotification> getRecentNotifications() {
        return notificationRepository.findTop50ByOrderByTimeStampDesc();
    }
}

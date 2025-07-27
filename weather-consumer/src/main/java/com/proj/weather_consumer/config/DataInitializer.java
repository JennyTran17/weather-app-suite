package com.proj.weather_consumer.config;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.proj.weather_consumer.model.User;
import com.proj.weather_consumer.model.WeatherEvent;
import com.proj.weather_consumer.model.WeatherNotification;
import com.proj.weather_consumer.repository.UserRepository;
import com.proj.weather_consumer.repository.WeatherNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final WeatherNotificationRepository notificationRepository;

    public DataInitializer(UserRepository userRepository, WeatherNotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize users
        User alice = new User();
        alice.setUserName("Alice");
        alice.setEmail("alice@example.com");
        alice.setCity("Athlone, IE");
        alice.setNotificationPreference("rain");
        userRepository.save(alice);

        User bob = new User();
        bob.setUserName("Bob");
        bob.setEmail("bob@example.com");
        bob.setCity("Athlone, IE");
        bob.setNotificationPreference("clear");
        userRepository.save(bob);

        User charlie = new User();
        charlie.setUserName("Charlie");
        charlie.setEmail("charlie@example.com");
        charlie.setCity("New York, US");
        charlie.setNotificationPreference("rain");
        userRepository.save(charlie);

        LOGGER.info("Initialized sample users in the database.");
        LOGGER.info("Weather notifications will be populated from real Athlone weather data via Open-Meteo API.");
    }
}

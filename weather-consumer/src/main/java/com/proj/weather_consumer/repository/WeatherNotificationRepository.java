package com.proj.weather_consumer.repository;

import com.proj.weather_consumer.model.WeatherNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherNotificationRepository extends JpaRepository<WeatherNotification, Long> {
    List<WeatherNotification> findTop50ByOrderByTimeStampDesc();
}
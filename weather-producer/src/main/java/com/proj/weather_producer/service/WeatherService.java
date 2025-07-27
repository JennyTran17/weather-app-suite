package com.proj.weather_producer.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.proj.weather_producer.model.WeatherEvent;
import com.proj.weather_producer.producer.WeatherEventProducer;
import com.proj.weather_producer.dto.ApiResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WeatherService {
    @Value("${open-meteo.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final WeatherEventProducer weatherEventProducer;

    public WeatherService(WeatherEventProducer weatherEventProducer) {
        this.weatherEventProducer = weatherEventProducer;
    }

    @Scheduled(fixedRate = 600000) // Polls every 10 minutes
    public void fetchAndPublishWeather() {
        System.out.println("Fetching weather data from Open-Meteo API: " + apiUrl);
        try{
            ApiResponse response = restTemplate.getForObject(apiUrl, ApiResponse.class);
            if (response != null && response.getHourly() != null) {
                System.out.println("Successfully received weather data from Open-Meteo API");
                // Find the index for the current hour
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                int currentIndex = -1;

                for (int i = 0; i < response.getHourly().getTime().size(); i++) {
                    LocalDateTime dataTime = LocalDateTime.parse(response.getHourly().getTime().get(i), formatter);
                    if (dataTime.getHour() == now.getHour()) {
                        currentIndex = i;
                        break;
                    }
                }

                if (currentIndex != -1) {
                    double precipitation = response.getHourly().getPrecipitation().get(currentIndex);
                    double temp = response.getHourly().getTemperature2m().get(currentIndex);

                    String condition;
                    if (precipitation > 0.5) { // A threshold to determine "rain"
                        condition = "rain";
                    } else {
                        condition = "clear";
                    }

                    // Publish the event to Kafka only if it's rain or clear
                    // Assuming city is Athlone for simplicity based on coordinates
                    System.out.println(String.format("Publishing Athlone weather: %s, %.1f°C, precipitation: %.1fmm", condition, temp, precipitation));
                    WeatherEvent event = new WeatherEvent("Athlone, IE", condition, temp, LocalDateTime.now());
                    weatherEventProducer.sendWeatherEvent(event);
                } else {
                    System.out.println("No current hour weather data found in API response");
                }
            } else {
                System.out.println("No weather data received from Open-Meteo API");
            }
        } catch (Exception e) {
            System.err.println("Error fetching weather data from Open-Meteo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

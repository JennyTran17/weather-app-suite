package com.proj.weather_consumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.proj.weather_consumer.model.WeatherEvent;
import com.proj.weather_consumer.service.NotificationService;

@Component
public class WeatherEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(WeatherEventConsumer.class);
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    // We inject our new components here
    public WeatherEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "weather-events", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String event) {
        System.out.println("Received weather event: {}"+ event);

        try{
            //Deserialize the string to local WeatherEvent
            WeatherEvent weatherEvent = objectMapper.readValue(event, WeatherEvent.class);
            // Find users who want to be notified about this weather condition
            String notificationMessage = String.format(
                    "New weather alert for %s: %s, %.1f°C",
                    weatherEvent.getCity(), weatherEvent.getCondition(), weatherEvent.getTemperatureCelsius()
            );
            WeatherEvent notification = new WeatherEvent(
                    weatherEvent.getCity(), weatherEvent.getCondition(), weatherEvent.getTemperatureCelsius(), notificationMessage, weatherEvent.getTimeStamp()
            );
            notificationService.sendWeatherNotification(notification);

            // Send notifications
            notificationService.sendWeatherNotification(notification);
            System.out.println(notificationMessage);


        } catch (JsonProcessingException e) {
            // 3. If it's a corrupted message (JsonParseException is a subclass of JsonProcessingException),
            //    catch the exception, log it, and DO NOT re-throw it.
            //    This effectively discards the message without triggering Kafka retries.
            log.error("Corrupted message received. Discarding without retry. Payload: '{}'. Error: {}",
                    event, e.getMessage());

        } catch (Exception e) {
            log.error("Error processing order event (retry/DLT if configured) [{}]: {}", event, e.getMessage(), e);
            // In a real application, you'd handle this more robustly:
            // - Send to a Dead Letter Queue (DLQ)
            // - Log detailed error
            // - Implement retry mechanism

        }




    }
}

package com.proj.weather_producer.producer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.proj.weather_producer.model.WeatherEvent;

@Service
public class WeatherEventProducer {
    private static final String TOPIC = "weather-events";
    private final KafkaTemplate<String, WeatherEvent> kafkaTemplate;

    public WeatherEventProducer(KafkaTemplate<String, WeatherEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendWeatherEvent(WeatherEvent event) {
        kafkaTemplate.send(TOPIC, event);
        System.out.println("Published weather event to Kafka: " + event.getCondition());
    }
}

package com.proj.weather_producer;

import com.proj.weather_producer.model.WeatherEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
@TestPropertySource(properties = {
		"spring.kafka.bootstrap-servers="
})
class WeatherProducerApplicationTests {

	@MockBean
	private KafkaTemplate<String, WeatherEvent> kafkaTemplate;

	@Test
	void contextLoads() {
	}

}

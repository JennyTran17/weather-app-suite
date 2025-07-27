package com.proj.weather_consumer.config;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer; //use String instead of Object OrderRequest
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff; //for retries on DLT


import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);


    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${kafka.topic.dlt.weather-events:weather-events.DLT}")
    private String weatherEventsDltTopic; //new property for Dead Letter Topic

    @PostConstruct //method run after bean initialization
    public void init() {
       log.info("Configured DLT topic: {}", weatherEventsDltTopic);
    }


    @Bean
    // This method defines a Spring Bean that provides Kafka consumer instances.
    // The <String, String> generic types indicate that Kafka messages
    // will have String keys and String values (the message payload)

    // The ConsumerFactory now produces String keys and String values
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        // Configure ErrorHandlingDeserializer to wrap StringDeserializer
        // If StringDeserializer fails, ErrorHandlingDeserializer catches it.
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Tell ErrorHandlingDeserializer which concrete deserializer to use
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class);


        // Return a DefaultKafkaConsumerFactory with StringDeserializer for both key and value

        // This is the crucial part of our solution for the deserialization issue:
        // We are telling Kafka to use StringDeserializer for both the key and the value.
        // This means the message payload will arrive in our @KafkaListener method
        // as a raw JSON String, not a deserialized Java object.
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(), // Key Deserializer
                new StringDeserializer()  // Value Deserializer (messages arrive as raw JSON strings)
        );
    }

    @Bean
    // This method defines a Spring Bean that creates and configures the container
    // for @KafkaListener annotated methods. It uses the ConsumerFactory defined above.
    // It manages the threading model for consuming messages concurrently.

    //It's responsible for managing the lifecycle of the consumer, polling for messages, and dispatching them to your listener methods
    // The KafkaListenerContainerFactory also now handles String keys and String values
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            // Correctly inject the ConsumerFactory created above
            final ConsumerFactory<String, String> consumerFactory)
    {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();


        // Use the injected consumerFactory

        // This line links the container factory to our specific consumer configuration.
        // It tells the factory to use the consumerFactory bean to create consumers,
        // ensuring they use StringDeserializer for values.
        factory.setConsumerFactory(consumerFactory);

        // This tells Spring to use 2 listener threads.
        // Each thread can process a message from a different partition,
        // allowing them to run concurrently.
        factory.setConcurrency(2);

        // --- Configure DefaultErrorHandler for DLT and Retries (within Kafka) ---
        // This handler will apply to any exception thrown from your listener,
        // including deserialization errors caught by ErrorHandlingDeserializer
        // AND any exceptions during processing that are not handled by @Retryable
        // or that exhaust @Retryable's attempts.
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (consumerRecord, exception) -> {
                    log.error("Message {} from topic {} failed after all retries and will be sent to DLT {}. Exception: {}",
                            consumerRecord.value(), consumerRecord.topic(), weatherEventsDltTopic, exception.getMessage(), exception);
                },
                new FixedBackOff(1000L, 2L) // Retry 3 times (initial + 2 retries) with 1 sec delay before sending to DLT
        );

        //For JsonParseException (corrupted messages), discard them immediately
        errorHandler.addNotRetryableExceptions(JsonParseException.class);

        //For other exceptions, it will retry then send to DLT
        errorHandler.addRetryableExceptions(RuntimeException.class);

        factory.setCommonErrorHandler(errorHandler); // Apply the error handler

        // It's often good practice to manually acknowledge messages after processing,
        // especially with DLTs, so you have control over when the offset is committed.
        factory.getContainerProperties().setAckMode(AckMode.RECORD);

        return factory;
    }
}
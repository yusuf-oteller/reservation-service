package com.hotelapp.reservation.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic reservationCreatedTopic() {
        return TopicBuilder.name("reservation-created-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic reservationCreatedDeadLetterTopic() {
        return TopicBuilder.name("dead_letter_topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentResultTopic() {
        return TopicBuilder.name("payment-result-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentResultDeadLetterTopic() {
        return TopicBuilder.name("payment-result-dead-letter")
                .partitions(1)
                .replicas(1)
                .build();
    }
}

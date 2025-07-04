package com.hotelapp.reservation.kafka;

import com.hotelapp.reservation.event.ReservationCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducer {

    private final KafkaTemplate<String, ReservationCreatedEvent> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, ReservationCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(ReservationCreatedEvent event) {
        log.info("Sending Kafka event: {}", event);
        kafkaTemplate.send("reservation-created-topic", event);
    }
}

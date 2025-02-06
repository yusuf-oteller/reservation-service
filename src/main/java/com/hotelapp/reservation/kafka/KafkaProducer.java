package com.hotelapp.reservation.kafka;

import com.hotelapp.reservation.event.ReservationCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, ReservationCreatedEvent> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, ReservationCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(ReservationCreatedEvent event) {
        System.out.println("Kafka'ya gönderilen mesaj: " + event.toString());
        kafkaTemplate.send("reservation-created-topic", event);
    }
}

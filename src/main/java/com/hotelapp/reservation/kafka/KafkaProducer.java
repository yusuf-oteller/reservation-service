package com.hotelapp.reservation.kafka;

import com.hotelapp.reservation.event.ReservationCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducer {

    private static final String MAIN_TOPIC = "reservation-created-topic";
    private static final String DEAD_LETTER_TOPIC = "reservation-created-dead-letter";

    private final KafkaTemplate<String, ReservationCreatedEvent> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, ReservationCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(ReservationCreatedEvent event) {
        log.info("Sending Kafka event: {}", event);

        kafkaTemplate.send(MAIN_TOPIC, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka message failed after retries. Sending to DLQ. Reason: {}", ex.getMessage(), ex);
                        sendToDeadLetterQueue(event);
                    } else {
                        log.info("Message sent successfully: {}", result.getRecordMetadata().toString());
                    }
                });
    }

    private void sendToDeadLetterQueue(ReservationCreatedEvent event) {
        kafkaTemplate.send(DEAD_LETTER_TOPIC, event)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send to Dead Letter Queue. Manual intervention required. Reason: {}", ex.getMessage(), ex);
                    } else {
                        log.warn("Event sent to Dead Letter Queue successfully.");
                    }
                });
    }
}

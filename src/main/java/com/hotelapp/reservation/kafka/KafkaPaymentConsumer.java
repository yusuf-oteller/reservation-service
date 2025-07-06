package com.hotelapp.reservation.kafka;

import com.hotelapp.reservation.event.PaymentResultEvent;
import com.hotelapp.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPaymentConsumer {

    private static final String DLQ_TOPIC = "payment-result-dead-letter";

    private final ReservationService reservationService;
    private final KafkaTemplate<String, PaymentResultEvent> kafkaTemplate;

    @KafkaListener(topics = "payment-result-topic", groupId = "reservation-service-group")
    public void handlePaymentResult(PaymentResultEvent event) {
        log.info("Received payment result event: {}", event);

        try {
            reservationService.updateStatusByPaymentResult(event.getReservationId(), event.getStatus());
        } catch (Exception e) {
            log.error("Failed to process payment result event. Sending to DLQ. Reason: {}", e.getMessage(), e);
            sendToDeadLetterQueue(event);
        }
    }

    private void sendToDeadLetterQueue(PaymentResultEvent event) {
        kafkaTemplate.send(DLQ_TOPIC, event)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send to Dead Letter Queue. Manual intervention required. Reason: {}", ex.getMessage(), ex);
                    } else {
                        log.warn("Event sent to Dead Letter Queue successfully.");
                    }
                });
    }
}

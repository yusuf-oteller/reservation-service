package com.hotelapp.reservation.kafka;

import com.hotelapp.reservation.event.PaymentResultEvent;
import com.hotelapp.reservation.model.PaymentStatus;
import com.hotelapp.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

public class KafkaPaymentConsumerTest {

    private ReservationService reservationService;
    private KafkaTemplate<String, PaymentResultEvent> kafkaTemplate;
    private KafkaPaymentConsumer consumer;

    @BeforeEach
    void setUp() {
        reservationService = mock(ReservationService.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        consumer = new KafkaPaymentConsumer(reservationService, kafkaTemplate);
    }

    @Test
    void handlePaymentResult_shouldCallService() {
        PaymentResultEvent event = PaymentResultEvent.builder()
                .reservationId(1L)
                .status(PaymentStatus.SUCCESS)
                .transactionId("tx123")
                .maskedCardNumber("**** **** **** 1234")
                .amount(new BigDecimal("100.00"))
                .paidAt(LocalDateTime.now())
                .build();

        consumer.handlePaymentResult(event);

        verify(reservationService).updateStatusByPaymentResult(1L, PaymentStatus.SUCCESS);
    }

    @Test
    void handlePaymentResult_shouldSendToDLQ_onException() {
        PaymentResultEvent event = PaymentResultEvent.builder()
                .reservationId(1L)
                .status(PaymentStatus.FAILED)
                .build();

        doThrow(new RuntimeException("Simulated error"))
                .when(reservationService).updateStatusByPaymentResult(anyLong(), any());

        SendResult<String, PaymentResultEvent> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, PaymentResultEvent>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(eq("payment-result-dead-letter"), eq(event)))
                .thenReturn(future);

        consumer.handlePaymentResult(event);

        verify(kafkaTemplate).send(eq("payment-result-dead-letter"), eq(event));
    }

}
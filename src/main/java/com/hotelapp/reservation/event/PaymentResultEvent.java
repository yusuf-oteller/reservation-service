package com.hotelapp.reservation.event;

import com.hotelapp.reservation.model.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResultEvent {
    private Long reservationId;
    private PaymentStatus status; // SUCCESS, FAILED
    private String transactionId;
    private String maskedCardNumber;
    private BigDecimal amount;
    private LocalDateTime paidAt;
}

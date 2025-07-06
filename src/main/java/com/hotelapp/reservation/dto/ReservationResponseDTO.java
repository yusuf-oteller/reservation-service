package com.hotelapp.reservation.dto;

import com.hotelapp.reservation.model.ReservationStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponseDTO {

    private Long id;
    private Long hotelId;
    private Long roomId;
    private String guestName;
    private String userId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private ReservationStatus status;
    private LocalDateTime createdAt;
}

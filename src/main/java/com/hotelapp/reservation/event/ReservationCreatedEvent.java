package com.hotelapp.reservation.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hotelapp.reservation.model.Reservation;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReservationCreatedEvent {
    private Long reservationId;
    private Long hotelId;
    private Long roomId;
    private String guestName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    public ReservationCreatedEvent(Reservation reservation) {
        this.reservationId = reservation.getId();
        this.hotelId = reservation.getHotelId();
        this.roomId = reservation.getRoomId();
        this.guestName = reservation.getGuestName();
        this.checkInDate = reservation.getCheckInDate();
        this.checkOutDate = reservation.getCheckOutDate();
    }
}

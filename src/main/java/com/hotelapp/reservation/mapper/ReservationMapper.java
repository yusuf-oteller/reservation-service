package com.hotelapp.reservation.mapper;

import com.hotelapp.reservation.dto.ReservationRequestDTO;
import com.hotelapp.reservation.dto.ReservationResponseDTO;
import com.hotelapp.reservation.model.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public Reservation toEntity(ReservationRequestDTO dto) {
        return Reservation.builder()
                .hotelId(dto.getHotelId())
                .roomId(dto.getRoomId())
                .guestName(dto.getGuestName())
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .build();
    }

    public ReservationResponseDTO toResponseDTO(Reservation reservation) {
        return ReservationResponseDTO.builder()
                .id(reservation.getId())
                .hotelId(reservation.getHotelId())
                .roomId(reservation.getRoomId())
                .guestName(reservation.getGuestName())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}

package com.hotelapp.reservation.mapper;

import com.hotelapp.reservation.dto.ReservationRequestDTO;
import com.hotelapp.reservation.dto.ReservationResponseDTO;
import com.hotelapp.reservation.model.Reservation;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    Reservation toEntity(ReservationRequestDTO dto);

    ReservationResponseDTO toResponseDTO(Reservation reservation);
}

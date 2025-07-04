package com.hotelapp.reservation.controller;

import com.hotelapp.reservation.dto.ApiResponse;
import com.hotelapp.reservation.dto.ReservationRequestDTO;
import com.hotelapp.reservation.dto.ReservationResponseDTO;
import com.hotelapp.reservation.mapper.ReservationMapper;
import com.hotelapp.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    public ReservationController(ReservationService reservationService, ReservationMapper reservationMapper) {
        this.reservationService = reservationService;
        this.reservationMapper = reservationMapper;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> createReservation(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ReservationRequestDTO dto) {

        log.info("Received reservation request from user: {}", userId);
        ReservationResponseDTO reservation = reservationService.createReservation(userId, dto);
        return ResponseEntity.ok(ApiResponse.success(reservation, "Reservation created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponseDTO>>> getUserReservations(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("Fetching reservations for user: {}, role: {}", userId, role);
        List<ReservationResponseDTO> reservations = "ROLE_ADMIN".equals(role)
                ? reservationService.getAllReservations()
                : reservationService.getReservationsByUser(userId);

        return ResponseEntity.ok(ApiResponse.success(reservations, "Reservations fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> getReservationById(@PathVariable Long id) {
        log.info("Getting reservation by ID: {}", id);
        return reservationService.getReservationById(id)
                .map(reservationMapper::toResponseDTO)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Reservation found")))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Reservation not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("Deleting reservation with ID: {}", id);
        reservationService.deleteReservation(id, userId, role);

        return ResponseEntity.ok(ApiResponse.success(null, "Reservation deleted successfully"));
    }
}

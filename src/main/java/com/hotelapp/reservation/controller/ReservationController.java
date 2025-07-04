package com.hotelapp.reservation.controller;

import com.hotelapp.reservation.dto.ReservationRequestDTO;
import com.hotelapp.reservation.dto.ReservationResponseDTO;
import com.hotelapp.reservation.model.Reservation;
import com.hotelapp.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ReservationRequestDTO requestDTO) {

        requestDTO.setGuestName(requestDTO.getGuestName().trim()); // örnek sanitize
        ReservationResponseDTO response = reservationService.createReservation(userId, requestDTO);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<Reservation>> getUserReservations(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        List<Reservation> reservations;

        if ("ROLE_ADMIN".equals(role)) {
            reservations = reservationService.getAllReservations();
        } else {
            reservations = reservationService.getReservationsByUser(userId);
        }

        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Reservation>> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}

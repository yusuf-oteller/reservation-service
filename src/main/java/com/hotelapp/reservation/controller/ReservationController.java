package com.hotelapp.reservation.controller;

import com.hotelapp.reservation.model.Reservation;
import com.hotelapp.reservation.service.ReservationService;
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
    public ResponseEntity<Reservation> createReservation(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Reservation reservation) {

        reservation.setUserId(userId);
        return ResponseEntity.ok(reservationService.createReservation(reservation));
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

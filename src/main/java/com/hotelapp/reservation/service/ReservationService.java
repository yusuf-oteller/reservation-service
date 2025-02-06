package com.hotelapp.reservation.service;

import com.hotelapp.reservation.exception.ReservationConflictException;
import com.hotelapp.reservation.model.Reservation;
import com.hotelapp.reservation.repository.ReservationRepository;
import com.hotelapp.reservation.event.ReservationCreatedEvent;
import com.hotelapp.reservation.kafka.KafkaProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final KafkaProducer kafkaProducer;

    public ReservationService(ReservationRepository reservationRepository, KafkaProducer kafkaProducer) {
        this.reservationRepository = reservationRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional
    public Reservation createReservation(Reservation reservation) {
        List<Reservation> existingReservations = reservationRepository.findByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                reservation.getRoomId(),
                reservation.getCheckOutDate(),
                reservation.getCheckInDate()
        );

        if (!existingReservations.isEmpty()) {
            throw new ReservationConflictException("Bu oda seçilen tarihlerde zaten rezerve edilmiştir.");
        }

        reservation.setCreatedAt(LocalDateTime.now());
        Reservation savedReservation = reservationRepository.save(reservation);

        // Kafka Event Gönder
        kafkaProducer.sendMessage(new ReservationCreatedEvent(savedReservation));

        return savedReservation;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<Reservation> getReservationsByUser(String userId) {
        return reservationRepository.findByUserId(userId);
    }
}

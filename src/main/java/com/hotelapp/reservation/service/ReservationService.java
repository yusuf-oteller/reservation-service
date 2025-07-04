package com.hotelapp.reservation.service;

import com.hotelapp.reservation.dto.ReservationRequestDTO;
import com.hotelapp.reservation.dto.ReservationResponseDTO;
import com.hotelapp.reservation.exception.ReservationConflictException;
import com.hotelapp.reservation.mapper.ReservationMapper;
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
    private final ReservationMapper reservationMapper;

    public ReservationService(ReservationRepository reservationRepository,
                              KafkaProducer kafkaProducer,
                              ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.kafkaProducer = kafkaProducer;
        this.reservationMapper = reservationMapper;
    }

    @Transactional
    public ReservationResponseDTO createReservation(String userId, ReservationRequestDTO dto) {
        List<Reservation> existingReservations = reservationRepository.
                findByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                        dto.getRoomId(),
                        dto.getCheckOutDate(),
                        dto.getCheckInDate()
                );

        if (!existingReservations.isEmpty()) {
            throw new ReservationConflictException("Bu oda seçilen tarihlerde zaten rezerve edilmiştir.");
        }

        Reservation reservation = reservationMapper.toEntity(dto);
        reservation.setUserId(userId);
        reservation.setCreatedAt(LocalDateTime.now());

        Reservation saved = reservationRepository.save(reservation);

        kafkaProducer.sendMessage(new ReservationCreatedEvent(saved));

        return reservationMapper.toResponseDTO(saved);
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

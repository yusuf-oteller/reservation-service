package com.hotelapp.reservation.service;

import com.hotelapp.reservation.dto.ReservationRequestDTO;
import com.hotelapp.reservation.dto.ReservationResponseDTO;
import com.hotelapp.reservation.exception.ReservationConflictException;
import com.hotelapp.reservation.mapper.ReservationMapper;
import com.hotelapp.reservation.model.PaymentStatus;
import com.hotelapp.reservation.model.Reservation;
import com.hotelapp.reservation.model.ReservationStatus;
import com.hotelapp.reservation.repository.ReservationRepository;
import com.hotelapp.reservation.event.ReservationCreatedEvent;
import com.hotelapp.reservation.kafka.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Slf4j
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
        log.info("Creating reservation for user: {}, roomId: {}, checkIn: {}, checkOut: {}",
                userId, dto.getRoomId(), dto.getCheckInDate(), dto.getCheckOutDate());

        List<Reservation> existing = reservationRepository
                .findByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                        dto.getRoomId(), dto.getCheckOutDate(), dto.getCheckInDate());

        if (!existing.isEmpty()) {
            log.warn("Reservation conflict detected for roomId: {}, checkIn: {}, checkOut: {}",
                    dto.getRoomId(), dto.getCheckInDate(), dto.getCheckOutDate());
            throw new ReservationConflictException("Bu oda seçilen tarihlerde zaten rezerve edilmiştir.");
        }

        Reservation entity = reservationMapper.toEntity(dto);
        entity.setUserId(userId);
        entity.setCreatedAt(LocalDateTime.now());

        Reservation saved = reservationRepository.save(entity);
        log.info("Reservation saved with ID: {}", saved.getId());

        kafkaProducer.sendMessage(new ReservationCreatedEvent(saved));
        log.info("ReservationCreatedEvent sent to Kafka for reservation ID: {}", saved.getId());

        return reservationMapper.toResponseDTO(saved);
    }

    public List<ReservationResponseDTO> getAllReservations() {
        log.info("Fetching all reservations (admin access)");
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::toResponseDTO)
                .collect(toList());
    }

    public Optional<Reservation> getReservationById(Long id) {
        log.info("Fetching reservation by ID: {}", id);
        return reservationRepository.findById(id);
    }

    public void deleteReservation(Long id, String userId, String role) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);

        if (reservationOpt.isEmpty()) {
            throw new IllegalArgumentException("Reservation not found with id: " + id);
        }

        Reservation reservation = reservationOpt.get();

        if (!"ROLE_ADMIN".equals(role) && !reservation.getUserId().equals(userId)) {
            throw new SecurityException("You are not authorized to delete this reservation.");
        }

        reservationRepository.deleteById(id);
        log.info("Reservation deleted. id={}, by user={}", id, userId);
    }

    public List<ReservationResponseDTO> getReservationsByUser(String userId) {
        log.info("Fetching reservations for user: {}", userId);
        return reservationRepository.findByUserId(userId)
                .stream()
                .map(reservationMapper::toResponseDTO)
                .collect(toList());
    }

    @Transactional
    public void updateStatusByPaymentResult(Long reservationId, PaymentStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Reservation not found with id: " + reservationId));

        if (status == PaymentStatus.SUCCESS) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
        } else {
            reservation.setStatus(ReservationStatus.FAILED_PAYMENT);
        }

        reservationRepository.save(reservation);
        log.info("Reservation [{}] updated to status [{}] due to payment result", reservationId, reservation.getStatus());
    }
}
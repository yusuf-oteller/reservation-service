package com.hotelapp.reservation.service;

import com.hotelapp.reservation.dto.ReservationRequestDTO;
import com.hotelapp.reservation.dto.ReservationResponseDTO;
import com.hotelapp.reservation.event.ReservationCreatedEvent;
import com.hotelapp.reservation.exception.ReservationConflictException;
import com.hotelapp.reservation.kafka.KafkaProducer;
import com.hotelapp.reservation.mapper.ReservationMapper;
import com.hotelapp.reservation.model.PaymentStatus;
import com.hotelapp.reservation.model.Reservation;
import com.hotelapp.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private KafkaProducer kafkaProducer;
    private ReservationMapper reservationMapper;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        kafkaProducer = mock(KafkaProducer.class);
        reservationMapper = new ReservationMapper();
        reservationService = new ReservationService(reservationRepository, kafkaProducer, reservationMapper);
    }

    @Test
    void createReservation_shouldCreateAndSendEvent() {
        ReservationRequestDTO dto = ReservationRequestDTO.builder()
                .hotelId(1L)
                .roomId(101L)
                .guestName("Test Guest")
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(2))
                .build();

        when(reservationRepository.findByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any())).thenAnswer(invocation -> {
            Reservation res = invocation.getArgument(0);
            res.setId(10L);
            return res;
        });

        ReservationResponseDTO response = reservationService.createReservation("user-1", dto);

        assertEquals("Test Guest", response.getGuestName());
        assertEquals(1L, response.getHotelId());
        verify(kafkaProducer).sendMessage(any(ReservationCreatedEvent.class));
    }

    @Test
    void createReservation_shouldThrowConflictException() {
        Reservation existing = new Reservation();
        when(reservationRepository.findByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                anyLong(), any(), any())).thenReturn(Collections.singletonList(existing));

        ReservationRequestDTO dto = ReservationRequestDTO.builder()
                .hotelId(1L)
                .roomId(101L)
                .guestName("Test Guest")
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(2))
                .build();

        assertThrows(ReservationConflictException.class,
                () -> reservationService.createReservation("user-1", dto));
    }

    @Test
    void updateStatusByPaymentResult_shouldUpdateReservationStatus() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.updateStatusByPaymentResult(1L, PaymentStatus.SUCCESS);
        assertEquals("CONFIRMED", reservation.getStatus().name());
    }
}
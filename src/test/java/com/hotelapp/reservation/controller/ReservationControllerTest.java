package com.hotelapp.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelapp.reservation.dto.ReservationRequestDTO;
import com.hotelapp.reservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createReservation_shouldReturn200() throws Exception {
        ReservationRequestDTO dto = ReservationRequestDTO.builder()
                .hotelId(1L)
                .roomId(101L)
                .guestName("Test Guest")
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(1))
                .build();

        mockMvc.perform(post("/api/v1/reservations")
                        .header("X-User-Id", "user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserReservations_shouldReturn200() throws Exception {
        when(reservationService.getReservationsByUser("user-123")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/reservations")
                        .header("X-User-Id", "user-123")
                        .header("X-User-Role", "ROLE_CUSTOMER"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReservation_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/v1/reservations/10")
                        .header("X-User-Id", "user-123")
                        .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk());

        verify(reservationService).deleteReservation(10L, "user-123", "ROLE_ADMIN");
    }
}
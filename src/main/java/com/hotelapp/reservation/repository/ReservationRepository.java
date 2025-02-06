package com.hotelapp.reservation.repository;

import com.hotelapp.reservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reservation> findByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
            Long roomId, LocalDate checkOutDate, LocalDate checkInDate);

    List<Reservation> findByUserId(String userId);

}

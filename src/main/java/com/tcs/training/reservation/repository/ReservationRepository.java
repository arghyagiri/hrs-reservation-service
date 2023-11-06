package com.tcs.training.reservation.repository;

import com.tcs.training.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

	List<Reservation> findByCustomerId(Long customerId);

}

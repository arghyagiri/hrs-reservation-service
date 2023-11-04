package com.tcs.training.payment.repository;

import com.tcs.training.payment.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}

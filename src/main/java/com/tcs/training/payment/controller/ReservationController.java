package com.tcs.training.payment.controller;

import com.tcs.training.payment.entity.Reservation;
import com.tcs.training.payment.repository.ReservationRepository;
import com.tcs.training.payment.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("reservations")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	private final ReservationRepository reservationRepository;

	@GetMapping("/{id}")
	public Reservation getPaymentDetailsByUId(@PathVariable Long id) {
		return reservationRepository.getReferenceById(id);
	}

	@PostMapping("/make-reservation")
	public Reservation makeReservation(@RequestBody Reservation reservation) {
		return reservationService.makeReservation(reservation);
	}

	@PostMapping("/cancel-reservation/{id}")
	public String cancelReservation(@PathVariable("id") Long reservationId) {
		reservationService.cancelReservation(reservationId);
		return "Cancellation completed. Refund initiated";
	}

}

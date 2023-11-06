package com.tcs.training.reservation.controller;

import com.tcs.training.reservation.entity.Reservation;
import com.tcs.training.reservation.repository.ReservationRepository;
import com.tcs.training.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("reservations")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	private final ReservationRepository reservationRepository;

	@GetMapping("/{id}")
	public Reservation getReservationById(@PathVariable UUID id) {
		return reservationRepository.getReferenceById(id);
	}

	@PostMapping("/make-reservation")
	public Reservation makeReservation(@RequestBody Reservation reservation) {
		return reservationService.makeReservation(reservation);
	}

	@PostMapping("/update-reservation")
	public Reservation updateReservation(@RequestBody Reservation reservation) {
		return reservationService.updateReservation(reservation);
	}

	@PostMapping("/cancel-reservation/{id}")
	public String cancelReservation(@PathVariable("id") UUID reservationId) {
		reservationService.cancelReservation(reservationId);
		return "Cancellation completed. Refund initiated";
	}

	@GetMapping("/customer/{id}")
	public List<Reservation> getReservationForCustomer(@PathVariable("id") Long customerId) {
		return reservationRepository.findByCustomerId(customerId);
	}

}

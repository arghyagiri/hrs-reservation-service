package com.tcs.training.reservation.entity;

import com.tcs.training.reservation.feign.model.PaymentStatus;
import com.tcs.training.reservation.model.PaymentDetails;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "RESERVATIONS")
public class Reservation implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID reservationId;

	@Column(nullable = false)
	private Long customerId;

	@Column(nullable = false)
	private Long roomId;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	private UUID paymentId;

	private BigDecimal rentRatePerNight;

	private BigDecimal totalRent;

	private PaymentStatus paymentStatus;

	@Transient
	private PaymentDetails paymentDetails;

}

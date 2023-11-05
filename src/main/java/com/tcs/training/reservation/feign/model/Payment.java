package com.tcs.training.reservation.feign.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {

	private UUID paymentId;

	private Long customerId;

	private BigDecimal amount;

	private PaymentStatus paymentStatus;

}

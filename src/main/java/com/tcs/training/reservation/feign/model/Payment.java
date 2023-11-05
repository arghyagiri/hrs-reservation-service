package com.tcs.training.reservation.feign.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

	private PaymentChannel paymentChannel;

	private PaymentType paymentType;

}

package com.tcs.training.reservation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDetails {

	private String cardNumber;

	private LocalDate expiryDate;

	private Long cvv;

}

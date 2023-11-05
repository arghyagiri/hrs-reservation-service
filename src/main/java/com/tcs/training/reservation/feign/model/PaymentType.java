package com.tcs.training.reservation.feign.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PaymentType {

	PAY("PAY"), REFUND("REFUND");

	private final String name;

	public String toString() {
		return this.name;
	}

}

package com.tcs.training.reservation.feign.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {

	private Long customerId;

	private String firstName;

	private String lastName;

	private String emailAddress;

	private String contactNumber;

	private String address1;

	private String address2;

}

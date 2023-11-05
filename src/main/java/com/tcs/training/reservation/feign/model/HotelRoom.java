package com.tcs.training.reservation.feign.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotelRoom {

	private Long roomId;

	private Long customerId;

	private BigDecimal rent;

	private String roomStatus;

	private String roomType;

}

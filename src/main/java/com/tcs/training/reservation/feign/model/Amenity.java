package com.tcs.training.reservation.feign.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Amenity {

	private Long noOfAmenity;

	private AmenityDetails amenity;

}

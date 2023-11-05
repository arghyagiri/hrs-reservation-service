package com.tcs.training.reservation.feign.client;

import com.tcs.training.reservation.feign.exception.FeignClientErrorDecoder;
import com.tcs.training.reservation.feign.model.HotelListings;
import com.tcs.training.reservation.feign.model.HotelRoom;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "${feign.hotelClient.name}", configuration = FeignClientErrorDecoder.class, path = "/hotels")
public interface HotelClient {

	@GetMapping(value = "/listings")
	List<HotelListings> getHotelListings();

	@GetMapping("/{id}")
	public HotelListings getHotelById(@PathVariable Long id);

	@PostMapping("/reserve")
	public HotelRoom reserve(@RequestBody HotelRoom hotelRoom);

	@PostMapping("/un-reserve")
	public HotelRoom unReserve(@RequestBody HotelRoom hotelRoom);

	@PostMapping("/book")
	public HotelRoom confirmBooking(@RequestBody HotelRoom hotelRoom);

}

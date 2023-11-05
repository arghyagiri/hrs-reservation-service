package com.tcs.training.reservation.feign.client;

import com.tcs.training.reservation.feign.exception.FeignClientErrorDecoder;
import com.tcs.training.reservation.feign.model.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "${feign.customerClient.name}", configuration = FeignClientErrorDecoder.class, path = "/customers")
public interface CustomerClient {

	@GetMapping(value = "/get-customer/{id}")
	Customer getCustomerByCustomerId(@PathVariable("id") Long customerId);

}

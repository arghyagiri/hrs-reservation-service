package com.tcs.training.reservation.feign.client;

import com.tcs.training.reservation.feign.exception.FeignClientErrorDecoder;
import com.tcs.training.reservation.feign.model.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "${feign.paymentClient.name}", configuration = FeignClientErrorDecoder.class, path = "/payments")
public interface PaymentClient {

	@PostMapping("/refund/{paymentId}")
	public Payment processRefund(@PathVariable("paymentId") UUID paymentId);

	@PostMapping("/pay")
	public Payment processPayment(@RequestBody Payment payment);

}

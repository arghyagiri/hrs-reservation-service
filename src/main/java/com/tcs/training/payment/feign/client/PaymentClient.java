package com.tcs.training.payment.feign.client;

import com.tcs.training.payment.feign.exception.FeignClientErrorDecoder;
import com.tcs.training.payment.feign.model.Customer;
import com.tcs.training.payment.feign.model.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(name = "${feign.paymentClient.name}", configuration = FeignClientErrorDecoder.class, path = "/payments")
public interface PaymentClient {

	@PostMapping("/refund/{paymentId}")
	public Payment processRefund(@PathVariable("paymentId") UUID paymentId);

}

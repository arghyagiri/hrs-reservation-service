package com.tcs.training.payment.service;

import com.fasterxml.jackson.databind.ser.std.UUIDSerializer;
import com.tcs.training.model.exception.NoDataFoundException;
import com.tcs.training.model.notification.NotificationContext;
import com.tcs.training.payment.entity.Reservation;
import com.tcs.training.payment.feign.client.CustomerClient;
import com.tcs.training.payment.feign.client.PaymentClient;
import com.tcs.training.payment.feign.model.Customer;
import com.tcs.training.payment.feign.model.Payment;
import com.tcs.training.payment.model.ReservationStatus;
import com.tcs.training.payment.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

	private final ReservationRepository reservationRepository;

	private final Serde<NotificationContext> jsonSerde;

	private final CustomerClient customerClient;

	private final PaymentClient paymentClient;

	@Value("${spring.cloud.stream.kafka.streams.binder.brokers}")
	private String bootstrapServer;

	@Transactional
	public Reservation makeReservation(Reservation reservation) {
		return reservationRepository.save(reservation);
	}

	@Transactional
	public void cancelReservation(Long reservationId) {
		Reservation reservation = reservationRepository.getReferenceById(reservationId);
		if (reservation != null) {
			reservationRepository.deleteById(reservationId);
			initiatePaymentRefund(reservation.getPaymentId());
			sendCancellationNotification(reservation);
		}
		else {
			throw new NoDataFoundException("Invalid registration id to process");
		}
	}

	private Payment initiatePaymentRefund(UUID paymentId) {
		return paymentClient.processRefund(paymentId);
	}

	public void sendCancellationNotification(Reservation reservation) {
		Customer customer = customerClient.getCustomerByCustomerId(reservation.getCustomerId());
		if (customer != null) {
			NotificationContext nc = new NotificationContext();
			nc.setBody(String.format("""
									Sorry to hear that you want to cancel your booking!\n
									Your booking ref #%s has been cancelled and payment refund initiated with ref #%s.
					Wish to see you soon again and give us opportunity to serve you better.\n
					Why don’t you follow us on [social media] as well?\n
					-Great Comfort Hotels
					""", reservation.getReservationId(), reservation.getPaymentId()));
			nc.setType("email");
			nc.setSeverity("Low");
			nc.setCreatedAt(new Date());
			Map<String, String> context = new HashMap<>();
			context.put("to", customer.getEmailAddress());
			context.put("sub", String.format("Reservation Cancellation [#%s]", reservation.getReservationId()));
			nc.setContext(context);
			publishEventToNotificationTopic(nc);
		}
		else {
			throw new NoDataFoundException(
					"Reservation cancellation failed due to invalid customer id #" + reservation.getCustomerId());
		}
	}

	private void publishEventToNotificationTopic(NotificationContext nc) {
		KafkaTemplate kafkaTemplate = new KafkaTemplate<>(
				orderJsonSerdeFactoryFunction.apply(jsonSerde.serializer(), bootstrapServer), true);
		kafkaTemplate.setDefaultTopic("notificationProcessor");
		kafkaTemplate.sendDefault(UUID.randomUUID(), nc);
	}

	BiFunction<Serializer<NotificationContext>, String, DefaultKafkaProducerFactory<UUID, NotificationContext>> orderJsonSerdeFactoryFunction = (
			orderSerde, bootstrapServer) -> new DefaultKafkaProducerFactory<>(
					Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer, ProducerConfig.RETRIES_CONFIG, 0,
							ProducerConfig.BATCH_SIZE_CONFIG, 16384, ProducerConfig.LINGER_MS_CONFIG, 1,
							ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
							UUIDSerializer.class, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, orderSerde.getClass()));

}

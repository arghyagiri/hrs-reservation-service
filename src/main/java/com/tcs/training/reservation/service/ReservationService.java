package com.tcs.training.reservation.service;

import com.tcs.training.model.exception.NoDataFoundException;
import com.tcs.training.model.notification.NotificationContext;
import com.tcs.training.reservation.entity.Reservation;
import com.tcs.training.reservation.feign.client.CustomerClient;
import com.tcs.training.reservation.feign.client.HotelClient;
import com.tcs.training.reservation.feign.client.PaymentClient;
import com.tcs.training.reservation.feign.model.*;
import com.tcs.training.reservation.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
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

	private final HotelClient hotelClient;

	@Value("${spring.cloud.stream.kafka.streams.binder.brokers}")
	private String bootstrapServer;

	@Transactional
	public Reservation makeReservation(Reservation reservation) {
		HotelRoom hotelRoom = hotelClient.reserve(
				HotelRoom.builder().customerId(reservation.getCustomerId()).roomId(reservation.getRoomId()).build());
		if (hotelRoom != null) {
			reservation.setRentRatePerNight(hotelRoom.getRent());
			BigDecimal totalRent = calculateTotalRent(reservation);
			reservation.setTotalRent(totalRent);
			Payment payment = paymentClient.processPayment(Payment.builder()
				.customerId(reservation.getCustomerId())
				.amount(totalRent)
				.paymentChannel(PaymentChannel.MASTERCARD)
				.paymentType(PaymentType.PAY)
				.build());
			if (payment != null) {
				reservation.setPaymentStatus(payment.getPaymentStatus());
				reservation.setPaymentId(payment.getPaymentId());
				if (PaymentStatus.SUCCESS.equals(payment.getPaymentStatus())) {
					hotelClient.confirmBooking(hotelRoom);
					reservation = reservationRepository.save(reservation);
					sendReservationNotification(reservation);
					return reservation;
				}
				else {
					hotelClient.unReserve(hotelRoom);
					throw new NoDataFoundException("Payment failed.");
				}
			}
			else {
				throw new NoDataFoundException("Payment failed.");
			}
		}
		throw new NoDataFoundException("Requested hotel room is no more available.");
	}

	private BigDecimal calculateTotalRent(Reservation reservation) {
		Long daysOfStay = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
		return reservation.getRentRatePerNight().multiply(BigDecimal.valueOf(daysOfStay));
	}

	@Transactional
	public void cancelReservation(UUID reservationId) {
		Reservation reservation = reservationRepository.getReferenceById(reservationId);
		if (reservation != null) {
			initiatePaymentRefund(reservation.getPaymentId());
			reservationRepository.deleteById(reservationId);
			hotelClient.unReserve(HotelRoom.builder()
				.roomId(reservation.getRoomId())
				.customerId(reservation.getCustomerId())
				.build());
			sendCancellationNotification(reservation);
		}
		else {
			throw new NoDataFoundException("Invalid registration id to process");
		}
	}

	private Payment initiatePaymentRefund(UUID paymentId) {
		return paymentClient.processRefund(paymentId);
	}

	public void sendReservationNotification(Reservation reservation) {
		Customer customer = customerClient.getCustomerByCustomerId(reservation.getCustomerId());
		if (customer != null) {
			NotificationContext nc = new NotificationContext();
			nc.setBody(String.format("""
					Thanks for choosing us as your comfort partner.
					You booking for room no %s is confirmed stating %s and ending %s.
					Why don’t you follow us on [social media] as well?\n
					-Great Comfort Hotels
					""", reservation.getRoomId(), reservation.getStartDate(), reservation.getEndDate()));
			nc.setType("email");
			nc.setSeverity("Low");
			nc.setCreatedAt(new Date());
			Map<String, String> context = new HashMap<>();
			context.put("to", customer.getEmailAddress());
			context.put("sub", String.format("Reservation Confirmed [#%s]", reservation.getReservationId()));
			nc.setContext(context);
			publishEventToNotificationTopic(nc);
		}
		else {
			throw new NoDataFoundException(
					"Reservation cancellation failed due to invalid customer id #" + reservation.getCustomerId());
		}
	}

	public void sendCancellationNotification(Reservation reservation) {
		Customer customer = customerClient.getCustomerByCustomerId(reservation.getCustomerId());
		if (customer != null) {
			NotificationContext nc = new NotificationContext();
			nc.setBody(String.format(
					"Your booking ref %s has been cancelled and payment refund initiated with ref %s."
							+ "Wish to see you soon again!\n-Great Comfort Hotels",
					reservation.getReservationId(), reservation.getPaymentId()));
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

	public Reservation updateReservation(Reservation reservation) {
		return reservationRepository.save(reservation);
	}

}

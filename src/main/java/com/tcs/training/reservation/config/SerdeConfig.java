package com.tcs.training.reservation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.training.model.notification.NotificationContext;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

@Component
public class SerdeConfig {

	@Bean
	public Serde<NotificationContext> orderJsonSerde() {
		return new JsonSerde<>(NotificationContext.class, new ObjectMapper());
	}

}

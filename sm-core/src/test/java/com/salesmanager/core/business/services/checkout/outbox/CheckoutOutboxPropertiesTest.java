package com.salesmanager.core.business.services.checkout.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@EnableConfigurationProperties(CheckoutOutboxProperties.class)
@TestPropertySource(properties = "checkout.outbox.dispatcher.interval-ms=7500")
class CheckoutOutboxPropertiesTest {

	@Autowired
	private CheckoutOutboxProperties properties;

	@Test
	void bindsDispatcherIntervalMsFromNestedPropertyKey() {
		assertThat(properties.getDispatcherIntervalMs()).isEqualTo(7500L);
		assertThat(properties.getDispatcher().getIntervalMs()).isEqualTo(7500L);
	}

}

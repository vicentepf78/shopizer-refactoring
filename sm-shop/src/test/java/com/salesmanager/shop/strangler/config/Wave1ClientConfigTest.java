package com.salesmanager.shop.strangler.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = Wave1ClientConfigTest.TestConfig.class)
@TestPropertySource(properties = {
		"wave1.strangler.enabled=true",
		"wave1.reference-service.base-url=http://reference-test:8081",
		"wave1.tax-service.base-url=http://tax-test:8082",
		"wave1.http.client.timeout-ms=2500"
})
class Wave1ClientConfigTest {

	@Configuration
	@EnableConfigurationProperties(Wave1Properties.class)
	static class TestConfig {
	}

	@Autowired
	private Wave1Properties properties;

	@Test
	void bindsWave1Properties() {
		assertThat(properties.getStrangler().isEnabled()).isTrue();
		assertThat(properties.getReferenceService().getBaseUrl()).isEqualTo("http://reference-test:8081");
		assertThat(properties.getTaxService().getBaseUrl()).isEqualTo("http://tax-test:8082");
		assertThat(properties.getHttp().getClient().getTimeoutMs()).isEqualTo(2500L);
	}
}

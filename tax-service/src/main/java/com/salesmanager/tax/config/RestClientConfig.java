package com.salesmanager.tax.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

	@Bean
	public RestTemplate referenceRestTemplate(
			RestTemplateBuilder builder,
			@Value("${wave1.http.client.timeout-ms:5000}") long timeoutMs) {
		Duration timeout = Duration.ofMillis(timeoutMs);
		return builder
				.setConnectTimeout(timeout)
				.setReadTimeout(timeout)
				.build();
	}
}

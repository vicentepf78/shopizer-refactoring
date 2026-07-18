package com.salesmanager.shop.strangler.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(Wave1Properties.class)
public class Wave1ClientConfig {

	@Bean
	@ConditionalOnProperty(name = "wave1.strangler.enabled", havingValue = "true")
	public RestTemplate wave1RestTemplate(RestTemplateBuilder builder, Wave1Properties properties) {
		Duration timeout = Duration.ofMillis(properties.getHttp().getClient().getTimeoutMs());
		return builder
				.setConnectTimeout(timeout)
				.setReadTimeout(timeout)
				.build();
	}
}

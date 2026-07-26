package com.salesmanager.shop.strangler.config;

import java.time.Duration;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.salesmanager.contracts.client.MerchantServiceClient;
import com.salesmanager.contracts.client.SearchIndexClient;
import com.salesmanager.shop.filter.CorrelationIdFilter;
import com.salesmanager.shop.strangler.merchant.CachedMerchantServiceClient;
import com.salesmanager.shop.strangler.merchant.MerchantServiceClientRestTemplateImpl;
import com.salesmanager.shop.strangler.search.SearchIndexClientRestTemplateImpl;

@Configuration
@EnableConfigurationProperties(Wave2Properties.class)
public class Wave2ClientConfig {

	@Bean
	@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
	public RestTemplate wave2RestTemplate(RestTemplateBuilder builder, Wave2Properties properties) {
		Duration timeout = Duration.ofMillis(properties.getHttp().getClient().getTimeoutMs());
		return builder
				.setConnectTimeout(timeout)
				.setReadTimeout(timeout)
				.additionalInterceptors(correlationInterceptor())
				.build();
	}

	@Bean
	@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
	public SearchIndexClient searchIndexClient(
			RestTemplate wave2RestTemplate,
			Wave2Properties properties) {
		return new SearchIndexClientRestTemplateImpl(wave2RestTemplate, properties);
	}

	@Bean
	@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
	public MerchantServiceClient merchantServiceClient(
			RestTemplate wave2RestTemplate,
			Wave2Properties properties) {
		MerchantServiceClient delegate = new MerchantServiceClientRestTemplateImpl(wave2RestTemplate, properties);
		long ttlSeconds = properties.getMerchantService().getCache().getTtlSeconds();
		if (ttlSeconds > 0) {
			return new CachedMerchantServiceClient(delegate, ttlSeconds);
		}
		return delegate;
	}

	static ClientHttpRequestInterceptor correlationInterceptor() {
		return (request, body, execution) -> {
			if (!request.getHeaders().containsKey(CorrelationIdFilter.HEADER)) {
				request.getHeaders().set(CorrelationIdFilter.HEADER, resolveCorrelationId());
			}
			return execution.execute(request, body);
		};
	}

	private static String resolveCorrelationId() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs instanceof ServletRequestAttributes) {
			HttpServletRequest servletRequest = ((ServletRequestAttributes) attrs).getRequest();
			String existing = servletRequest.getHeader(CorrelationIdFilter.HEADER);
			if (StringUtils.hasText(existing)) {
				return existing;
			}
		}
		return UUID.randomUUID().toString();
	}
}

package com.salesmanager.tax.config;

import java.time.Duration;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.salesmanager.tax.web.CorrelationIdFilter;

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
				.additionalInterceptors(correlationInterceptor())
				.build();
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

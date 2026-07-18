package com.salesmanager.shop.strangler.support;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@ConditionalOnProperty(name = "wave1.strangler.enabled", havingValue = "true")
public class StranglerRestClient {

	public static final String CORRELATION_HEADER = "X-Correlation-Id";

	private final RestTemplate wave1RestTemplate;

	public StranglerRestClient(RestTemplate wave1RestTemplate) {
		this.wave1RestTemplate = wave1RestTemplate;
	}

	public <T> T exchange(
			String url,
			HttpMethod method,
			Object body,
			ParameterizedTypeReference<T> responseType,
			boolean forwardAuthorization) {
		try {
			HttpEntity<?> entity = new HttpEntity<>(body, headers(forwardAuthorization));
			ResponseEntity<T> response = wave1RestTemplate.exchange(url, method, entity, responseType);
			return response.getBody();
		} catch (HttpStatusCodeException e) {
			HttpStatus status = HttpStatus.resolve(e.getRawStatusCode());
			if (status == null) {
				status = HttpStatus.BAD_GATEWAY;
			}
			throw new DownstreamHttpException(status, e.getResponseBodyAsString());
		} catch (ResourceAccessException e) {
			throw new ServiceUnavailableException("Downstream service unavailable: " + url, e);
		} catch (RestClientException e) {
			throw new ServiceUnavailableException("Downstream service call failed: " + url, e);
		}
	}

	public <T> T exchange(
			String url,
			HttpMethod method,
			Object body,
			Class<T> responseType,
			boolean forwardAuthorization) {
		try {
			HttpEntity<?> entity = new HttpEntity<>(body, headers(forwardAuthorization));
			ResponseEntity<T> response = wave1RestTemplate.exchange(url, method, entity, responseType);
			return response.getBody();
		} catch (HttpStatusCodeException e) {
			HttpStatus status = HttpStatus.resolve(e.getRawStatusCode());
			if (status == null) {
				status = HttpStatus.BAD_GATEWAY;
			}
			throw new DownstreamHttpException(status, e.getResponseBodyAsString());
		} catch (ResourceAccessException e) {
			throw new ServiceUnavailableException("Downstream service unavailable: " + url, e);
		} catch (RestClientException e) {
			throw new ServiceUnavailableException("Downstream service call failed: " + url, e);
		}
	}

	public void exchangeVoid(String url, HttpMethod method, Object body, boolean forwardAuthorization) {
		exchange(url, method, body, Void.class, forwardAuthorization);
	}

	HttpHeaders headers(boolean forwardAuthorization) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(CORRELATION_HEADER, resolveCorrelationId());
		if (forwardAuthorization) {
			String authorization = currentHeader(HttpHeaders.AUTHORIZATION);
			if (StringUtils.isNotBlank(authorization)) {
				headers.set(HttpHeaders.AUTHORIZATION, authorization);
			}
		}
		return headers;
	}

	String resolveCorrelationId() {
		String existing = currentHeader(CORRELATION_HEADER);
		return StringUtils.isNotBlank(existing) ? existing : UUID.randomUUID().toString();
	}

	private String currentHeader(String name) {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (!(attrs instanceof ServletRequestAttributes)) {
			return null;
		}
		HttpServletRequest request = ((ServletRequestAttributes) attrs).getRequest();
		return request != null ? request.getHeader(name) : null;
	}
}

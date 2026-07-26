package com.salesmanager.shop.strangler.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.salesmanager.shop.filter.CorrelationIdFilter;
import com.salesmanager.shop.store.api.exception.ErrorEntity;
import com.salesmanager.shop.store.api.exception.RestErrorHandler;

class RestErrorHandlerStranglerTest {

	private final RestErrorHandler handler = new RestErrorHandler();

	@AfterEach
	void clearMdc() {
		MDC.clear();
	}

	@Test
	void serviceUnavailable_mapsTo503Body() {
		MDC.put(CorrelationIdFilter.MDC_KEY, "corr-strangler-503");
		ErrorEntity body = handler.handleServiceUnavailable(
				new ServiceUnavailableException("Downstream service unavailable", new RuntimeException("refused")));

		assertThat(body.getErrorCode()).isEqualTo("503");
		assertThat(body.getMessage()).contains("Downstream service unavailable");
		assertThat(body.getCorrelationId()).isEqualTo("corr-strangler-503");
	}

	@Test
	void downstreamHttp_preservesStatus() {
		ResponseEntity<ErrorEntity> response = handler.handleDownstream(
				new DownstreamHttpException(HttpStatus.NOT_FOUND, "missing"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getErrorCode()).isEqualTo("404");
	}
}

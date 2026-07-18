package com.salesmanager.shop.strangler.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.salesmanager.shop.store.api.exception.ErrorEntity;
import com.salesmanager.shop.store.api.exception.RestErrorHandler;

class RestErrorHandlerStranglerTest {

	private final RestErrorHandler handler = new RestErrorHandler();

	@Test
	void serviceUnavailable_mapsTo503Body() {
		ErrorEntity body = handler.handleServiceUnavailable(
				new ServiceUnavailableException("Downstream service unavailable", new RuntimeException("refused")));

		assertThat(body.getErrorCode()).isEqualTo("503");
		assertThat(body.getMessage()).contains("Downstream service unavailable");
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

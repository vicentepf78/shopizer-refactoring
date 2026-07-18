package com.salesmanager.tax.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.salesmanager.core.business.exception.TaxClassInUseException;

class RestErrorHandlerTest {

	private final RestErrorHandler handler = new RestErrorHandler();

	@Test
	void handleTaxClassInUse() {
		Map<String, Object> body = handler.handleTaxClassInUse(new TaxClassInUseException(1L, 2L));
		assertThat(body.get("errorCode")).isEqualTo(TaxClassInUseException.ERROR_CODE);
		assertThat(body.get("taxClassId")).isEqualTo(1L);
	}

	@Test
	void handleNotFoundAndForbiddenAndValidation() {
		assertThat(handler.handleNotFound(new ResourceNotFoundException("nf")).get("errorCode")).isEqualTo("404");
		assertThat(handler.handleForbidden(new StoreForbiddenException("no")).get("errorCode")).isEqualTo("403");
		assertThat(handler.handleBadRequest(new ValidationException("bad")).get("message")).isEqualTo("bad");
		assertThat(handler.handleBadRequest(new OperationNotAllowedException("dup")).get("errorCode")).isEqualTo("400");
		assertThat(handler.handleService(new ServiceRuntimeException("x")).get("message")).isEqualTo("x");
	}
}

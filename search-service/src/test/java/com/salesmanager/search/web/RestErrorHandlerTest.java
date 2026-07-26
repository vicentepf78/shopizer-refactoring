package com.salesmanager.search.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import com.salesmanager.search.support.SearchUnavailableException;

class RestErrorHandlerTest {

	private final RestErrorHandler handler = new RestErrorHandler();

	@Test
	void mapsOpenSearchUnavailableTo503Body() {
		MDC.put(CorrelationIdFilter.MDC_KEY, "corr-123");
		try {
			var body = handler.handleSearchUnavailable(new SearchUnavailableException("down"));
			assertThat(body.get("error")).isEqualTo("OpenSearch unavailable");
			assertThat(body.get("correlationId")).isEqualTo("corr-123");
		} finally {
			MDC.remove(CorrelationIdFilter.MDC_KEY);
		}
	}
}

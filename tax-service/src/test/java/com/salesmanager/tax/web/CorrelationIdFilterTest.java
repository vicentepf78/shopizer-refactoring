package com.salesmanager.tax.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

	@AfterEach
	void clearMdc() {
		MDC.clear();
	}

	@Test
	void generatesIdWhenHeaderAbsent() throws Exception {
		CorrelationIdFilter filter = new CorrelationIdFilter();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> seen = new AtomicReference<>();

		filter.doFilter(request, response, (req, res) ->
				seen.set(((HttpServletRequest) req).getHeader(CorrelationIdFilter.HEADER)));

		assertThat(seen.get()).isNotBlank();
		assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo(seen.get());
	}

	@Test
	void propagatesExistingHeader() throws Exception {
		CorrelationIdFilter filter = new CorrelationIdFilter();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(CorrelationIdFilter.HEADER, "corr-tax-9");
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> seen = new AtomicReference<>();

		filter.doFilter(request, response, (req, res) ->
				seen.set(((HttpServletRequest) req).getHeader(CorrelationIdFilter.HEADER)));

		assertThat(seen.get()).isEqualTo("corr-tax-9");
		assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("corr-tax-9");
	}
}

package com.salesmanager.shop.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ensures every request has {@code X-Correlation-Id} (generate if absent) for Wave 1 hops.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Correlation-Id";
	public static final String MDC_KEY = "correlationId";

	private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String correlationId = request.getHeader(HEADER);
		boolean generated = !StringUtils.hasText(correlationId);
		if (generated) {
			correlationId = UUID.randomUUID().toString();
		}

		MDC.put(MDC_KEY, correlationId);
		response.setHeader(HEADER, correlationId);
		log.debug("correlationId={}", correlationId);

		HttpServletRequest effective = generated
				? new CorrelationHeaderRequest(request, correlationId)
				: request;
		try {
			filterChain.doFilter(effective, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}

	static final class CorrelationHeaderRequest extends HttpServletRequestWrapper {

		private final String correlationId;

		CorrelationHeaderRequest(HttpServletRequest request, String correlationId) {
			super(request);
			this.correlationId = correlationId;
		}

		@Override
		public String getHeader(String name) {
			if (HEADER.equalsIgnoreCase(name)) {
				return correlationId;
			}
			return super.getHeader(name);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			if (HEADER.equalsIgnoreCase(name)) {
				return Collections.enumeration(Collections.singletonList(correlationId));
			}
			return super.getHeaders(name);
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			java.util.Set<String> names = new java.util.LinkedHashSet<>();
			Enumeration<String> original = super.getHeaderNames();
			while (original.hasMoreElements()) {
				names.add(original.nextElement());
			}
			names.add(HEADER);
			return Collections.enumeration(names);
		}
	}
}

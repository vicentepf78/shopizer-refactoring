package com.salesmanager.search.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.search.config.SearchServiceProperties;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class InternalTokenFilter extends OncePerRequestFilter {

	public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

	private final SearchServiceProperties properties;
	private final ObjectMapper objectMapper;

	public InternalTokenFilter(SearchServiceProperties properties, ObjectMapper objectMapper) {
		this.properties = properties;
		this.objectMapper = objectMapper;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path == null || !path.startsWith("/internal/v1/");
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String token = request.getHeader(INTERNAL_TOKEN_HEADER);
		String expected = properties.getInternal().getToken();
		if (!StringUtils.hasText(expected) || !expected.equals(token)) {
			writeUnauthorized(response);
			return;
		}
		filterChain.doFilter(request, response);
	}

	private void writeUnauthorized(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		java.util.Map<String, String> body = new java.util.HashMap<>();
		body.put("error", "Invalid or missing internal token");
		String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
		if (StringUtils.hasText(correlationId)) {
			body.put("correlationId", correlationId);
		}
		objectMapper.writeValue(response.getWriter(), body);
	}
}

package com.salesmanager.search.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.salesmanager.search.support.SearchUnavailableException;
import com.salesmanager.search.support.UnsupportedSchemaVersionException;

@RestControllerAdvice(basePackages = "com.salesmanager.search")
public class RestErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(RestErrorHandler.class);

	@ExceptionHandler(SearchUnavailableException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ResponseBody
	public Map<String, String> handleSearchUnavailable(SearchUnavailableException ex) {
		log.warn(ex.getMessage(), ex);
		return errorBody("OpenSearch unavailable");
	}

	@ExceptionHandler(UnsupportedSchemaVersionException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ResponseBody
	public Map<String, Object> handleUnsupportedSchema(UnsupportedSchemaVersionException ex) {
		Map<String, Object> body = new HashMap<>();
		body.put("error", ex.getMessage());
		body.put("schemaVersion", ex.getSchemaVersion());
		String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
		if (StringUtils.hasText(correlationId)) {
			body.put("correlationId", correlationId);
		}
		return body;
	}

	private Map<String, String> errorBody(String error) {
		Map<String, String> body = new HashMap<>();
		body.put("error", error);
		String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
		if (StringUtils.hasText(correlationId)) {
			body.put("correlationId", correlationId);
		}
		return body;
	}
}

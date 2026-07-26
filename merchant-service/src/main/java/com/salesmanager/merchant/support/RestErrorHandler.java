package com.salesmanager.merchant.support;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestControllerAdvice(basePackages = "com.salesmanager.merchant")
public class RestErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(RestErrorHandler.class);

	@ExceptionHandler({ ReferenceUnavailableException.class, ContentUnavailableException.class })
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ResponseBody
	public Map<String, Object> handleRemoteUnavailable(ServiceRuntimeException ex) {
		log.warn(ex.getErrorMessage(), ex);
		Map<String, Object> body = new HashMap<>();
		body.put("error", ex.getErrorCode());
		body.put("correlationId", correlationId());
		return body;
	}

	private static String correlationId() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs != null) {
			HttpServletRequest request = attrs.getRequest();
			if (request != null && request.getHeader("X-Correlation-Id") != null) {
				return request.getHeader("X-Correlation-Id");
			}
		}
		return UUID.randomUUID().toString();
	}
}

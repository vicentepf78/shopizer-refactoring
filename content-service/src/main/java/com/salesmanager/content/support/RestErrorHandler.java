package com.salesmanager.content.support;

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
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestControllerAdvice(basePackages = "com.salesmanager.content")
public class RestErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(RestErrorHandler.class);

	@ExceptionHandler(ReferenceUnavailableException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ResponseBody
	public Map<String, Object> handleReferenceUnavailable(ReferenceUnavailableException ex) {
		log.warn(ex.getErrorMessage(), ex);
		Map<String, Object> body = new HashMap<>();
		body.put("error", ex.getErrorCode());
		body.put("correlationId", correlationId());
		return body;
	}

	@ExceptionHandler(RestClientException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ResponseBody
	public Map<String, Object> handleRestClient(RestClientException ex) {
		log.warn("Remote call failed", ex);
		Map<String, Object> body = new HashMap<>();
		body.put("error", "SERVICE_UNAVAILABLE");
		body.put("correlationId", correlationId());
		return body;
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public Map<String, String> handleNotFound(ResourceNotFoundException ex) {
		return error(ex.getErrorCode(), ex.getErrorMessage());
	}

	@ExceptionHandler(ConstraintException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public Map<String, String> handleConstraint(ConstraintException ex) {
		return error(ex.getErrorCode(), ex.getErrorMessage());
	}

	@ExceptionHandler(StoreForbiddenException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ResponseBody
	public Map<String, String> handleForbidden(StoreForbiddenException ex) {
		return error(ex.getErrorCode(), ex.getErrorMessage());
	}

	@ExceptionHandler({ ValidationException.class, OperationNotAllowedException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Map<String, String> handleBadRequest(ServiceRuntimeException ex) {
		return error(ex.getErrorCode(), ex.getErrorMessage());
	}

	@ExceptionHandler(ServiceRuntimeException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Map<String, String> handleService(ServiceRuntimeException ex) {
		log.error(ex.getErrorMessage(), ex);
		return error(ex.getErrorCode(), ex.getErrorMessage());
	}

	private Map<String, String> error(String code, String message) {
		Map<String, String> body = new HashMap<>();
		body.put("errorCode", code);
		body.put("message", message);
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

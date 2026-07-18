package com.salesmanager.tax.support;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.salesmanager.core.business.exception.TaxClassInUseException;

@RestControllerAdvice(basePackages = "com.salesmanager.tax")
public class RestErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(RestErrorHandler.class);

	@ExceptionHandler(TaxClassInUseException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public Map<String, Object> handleTaxClassInUse(TaxClassInUseException ex) {
		log.warn(ex.getMessage());
		Map<String, Object> body = new HashMap<>();
		body.put("errorCode", TaxClassInUseException.ERROR_CODE);
		body.put("message", ex.getMessage());
		body.put("taxClassId", ex.getTaxClassId());
		body.put("productCount", ex.getProductCount());
		return body;
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public Map<String, String> handleNotFound(ResourceNotFoundException ex) {
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
}

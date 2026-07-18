package com.salesmanager.shop.strangler.support;

import com.salesmanager.shop.store.api.exception.GenericRuntimeException;

/**
 * Downstream connect/timeout while Strangler is enabled — maps to HTTP 503.
 */
public class ServiceUnavailableException extends GenericRuntimeException {

	private static final long serialVersionUID = 1L;

	public ServiceUnavailableException(String message, Throwable cause) {
		super("503", message, cause);
	}
}

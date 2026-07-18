package com.salesmanager.shop.strangler.support;

import org.springframework.http.HttpStatus;

import com.salesmanager.shop.store.api.exception.GenericRuntimeException;

/**
 * Propagates a non-2xx status from a Wave 1 downstream service.
 */
public class DownstreamHttpException extends GenericRuntimeException {

	private static final long serialVersionUID = 1L;

	private final HttpStatus status;

	public DownstreamHttpException(HttpStatus status, String message) {
		super(String.valueOf(status.value()), message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}

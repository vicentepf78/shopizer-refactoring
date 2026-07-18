package com.salesmanager.reference.support;

public class ServiceRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ServiceRuntimeException(String message) {
		super(message);
	}

	public ServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public ServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}

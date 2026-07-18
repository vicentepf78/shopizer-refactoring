package com.salesmanager.tax.support;

public class ValidationException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public ValidationException(String message) {
		super("400", message);
	}

	public ValidationException(String message, Throwable cause) {
		super("400", message);
		initCause(cause);
	}
}

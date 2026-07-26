package com.salesmanager.merchant.support;

public class ConversionRuntimeException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public ConversionRuntimeException(String message) {
		super("CONVERSION_ERROR", message);
	}

	public ConversionRuntimeException(Throwable cause) {
		super("CONVERSION_ERROR", cause.getMessage());
		initCause(cause);
	}
}

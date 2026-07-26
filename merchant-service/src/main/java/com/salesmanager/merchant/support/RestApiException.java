package com.salesmanager.merchant.support;

public class RestApiException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public RestApiException(String message) {
		super("400", message);
	}

	public RestApiException(Throwable cause) {
		super("400", cause.getMessage());
		initCause(cause);
	}
}

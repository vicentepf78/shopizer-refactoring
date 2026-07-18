package com.salesmanager.tax.support;

public class ServiceRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String errorCode;
	private final String errorMessage;

	public ServiceRuntimeException(String message) {
		super(message);
		this.errorCode = "500";
		this.errorMessage = message;
	}

	public ServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = "500";
		this.errorMessage = message;
	}

	public ServiceRuntimeException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
		this.errorMessage = message;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}

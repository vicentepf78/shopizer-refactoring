package com.salesmanager.merchant.support;

public class ReferenceUnavailableException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public ReferenceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public String getErrorCode() {
		return "REFERENCE_UNAVAILABLE";
	}
}

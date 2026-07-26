package com.salesmanager.merchant.support;

public class ContentUnavailableException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public ContentUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public String getErrorCode() {
		return "CONTENT_UNAVAILABLE";
	}
}

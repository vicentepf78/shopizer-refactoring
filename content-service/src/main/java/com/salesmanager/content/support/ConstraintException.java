package com.salesmanager.content.support;

public class ConstraintException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public ConstraintException(String message) {
		super(message);
	}

	@Override
	public String getErrorCode() {
		return "CONSTRAINT";
	}
}

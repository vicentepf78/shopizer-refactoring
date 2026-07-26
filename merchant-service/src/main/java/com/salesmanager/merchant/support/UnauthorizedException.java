package com.salesmanager.merchant.support;

public class UnauthorizedException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public UnauthorizedException() {
		super("401", "User not authorized");
	}

	public UnauthorizedException(String message) {
		super("401", message);
	}
}

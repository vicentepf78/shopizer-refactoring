package com.salesmanager.merchant.support;

public class ResourceNotFoundException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
		super("404", message);
	}
}

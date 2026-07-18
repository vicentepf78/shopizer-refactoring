package com.salesmanager.tax.support;

public class StoreForbiddenException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public StoreForbiddenException(String message) {
		super("403", message);
	}
}

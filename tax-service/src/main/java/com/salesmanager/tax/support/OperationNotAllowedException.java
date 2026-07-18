package com.salesmanager.tax.support;

public class OperationNotAllowedException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public OperationNotAllowedException(String message) {
		super("400", message);
	}
}

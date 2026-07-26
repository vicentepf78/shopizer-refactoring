package com.salesmanager.search.support;

public class SearchUnavailableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SearchUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public SearchUnavailableException(String message) {
		super(message);
	}
}

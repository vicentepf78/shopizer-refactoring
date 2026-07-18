package com.salesmanager.core.business.exception;

/**
 * Thrown when a TaxClass delete is blocked because products still reference it.
 * Mappable to HTTP 409 with body code {@code TAX_CLASS_IN_USE}.
 */
public class TaxClassInUseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static final String ERROR_CODE = "TAX_CLASS_IN_USE";

	private final Long taxClassId;
	private final long productCount;

	public TaxClassInUseException(Long taxClassId, long productCount) {
		super("Tax class [" + taxClassId + "] is referenced by " + productCount + " products");
		this.taxClassId = taxClassId;
		this.productCount = productCount;
	}

	public Long getTaxClassId() {
		return taxClassId;
	}

	public long getProductCount() {
		return productCount;
	}

	public String getErrorCode() {
		return ERROR_CODE;
	}
}

package com.salesmanager.shop.model.tax;

import com.salesmanager.shop.model.entity.Entity;

/**
 * @deprecated Use {@link com.salesmanager.contracts.tax.TaxRateEntity} from shopizer-api-contracts.
 * Kept as a compile-compatible legacy alias for the monolith.
 */
@Deprecated
public class TaxRateEntity extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int priority;
	private String code;
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

}

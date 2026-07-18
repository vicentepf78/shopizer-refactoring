package com.salesmanager.shop.model.tax;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Use {@link com.salesmanager.contracts.tax.ReadableTaxRateFull} from shopizer-api-contracts.
 * Kept as a compile-compatible legacy alias for the monolith.
 */
@Deprecated
public class ReadableTaxRateFull extends TaxRateEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<ReadableTaxRateDescription> descriptions = new ArrayList<ReadableTaxRateDescription>();
	public List<ReadableTaxRateDescription> getDescriptions() {
		return descriptions;
	}
	public void setDescriptions(List<ReadableTaxRateDescription> descriptions) {
		this.descriptions = descriptions;
	}
	


}

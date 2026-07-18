package com.salesmanager.contracts.tax;

import java.util.ArrayList;
import java.util.List;

public class ReadableTaxRateFull extends TaxRateEntity {

	private static final long serialVersionUID = 1L;

	private List<ReadableTaxRateDescription> descriptions = new ArrayList<ReadableTaxRateDescription>();

	public List<ReadableTaxRateDescription> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<ReadableTaxRateDescription> descriptions) {
		this.descriptions = descriptions;
	}

}

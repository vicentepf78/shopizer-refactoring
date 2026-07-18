package com.salesmanager.shop.model.references;

import java.util.List;

/**
 * @deprecated Use {@link com.salesmanager.contracts.reference.SizeReferences} from shopizer-api-contracts.
 * Kept as a compile-compatible legacy alias for the monolith.
 */
@Deprecated
public class SizeReferences {
	
	private List<WeightUnit> weights;
	private List<MeasureUnit> measures;
	public List<WeightUnit> getWeights() {
		return weights;
	}
	public void setWeights(List<WeightUnit> weights) {
		this.weights = weights;
	}
	public List<MeasureUnit> getMeasures() {
		return measures;
	}
	public void setMeasures(List<MeasureUnit> measures) {
		this.measures = measures;
	}

}

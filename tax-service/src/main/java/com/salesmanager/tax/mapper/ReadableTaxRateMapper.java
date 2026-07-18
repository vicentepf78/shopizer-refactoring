package com.salesmanager.tax.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import com.salesmanager.contracts.tax.ReadableTaxRate;
import com.salesmanager.contracts.tax.ReadableTaxRateDescription;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.tax.taxrate.TaxRate;
import com.salesmanager.core.model.tax.taxrate.TaxRateDescription;

@Component
public class ReadableTaxRateMapper {

	public ReadableTaxRate convert(TaxRate source, MerchantStore store, Language language) {
		ReadableTaxRate taxRate = new ReadableTaxRate();
		return merge(source, taxRate, store, language);
	}

	public ReadableTaxRate merge(TaxRate source, ReadableTaxRate destination, MerchantStore store, Language language) {
		Validate.notNull(destination, "destination TaxRate cannot be null");
		Validate.notNull(source, "source TaxRate cannot be null");
		destination.setId(source.getId());
		if (source.getCountry() != null) {
			destination.setCountry(source.getCountry().getIsoCode());
		}
		if (source.getZone() != null) {
			destination.setZone(source.getZone().getCode());
		}
		if (source.getTaxRate() != null) {
			destination.setRate(source.getTaxRate().toString());
		}
		destination.setCode(source.getCode());
		destination.setPriority(source.getTaxPriority() != null ? source.getTaxPriority() : 0);
		destination.setStore(store.getCode());
		Optional<ReadableTaxRateDescription> description = convertDescription(source.getDescriptions(), language);
		description.ifPresent(destination::setDescription);
		return destination;
	}

	private Optional<ReadableTaxRateDescription> convertDescription(
			List<TaxRateDescription> descriptions, Language language) {
		if (descriptions == null || descriptions.isEmpty() || language == null) {
			return Optional.empty();
		}
		return descriptions.stream()
				.filter(desc -> desc.getLanguage() != null
						&& language.getCode().equals(desc.getLanguage().getCode()))
				.findAny()
				.map(this::toReadable);
	}

	private ReadableTaxRateDescription toReadable(TaxRateDescription desc) {
		ReadableTaxRateDescription d = new ReadableTaxRateDescription();
		d.setDescription(desc.getDescription());
		d.setName(desc.getName());
		d.setLanguage(desc.getLanguage().getCode());
		d.setId(desc.getId());
		d.setTitle(desc.getTitle());
		return d;
	}
}

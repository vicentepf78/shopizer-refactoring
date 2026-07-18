package com.salesmanager.tax.mapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.contracts.tax.PersistableTaxRate;
import com.salesmanager.contracts.tax.TaxRateDescription;
import com.salesmanager.core.business.services.tax.TaxClassService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.core.model.tax.taxrate.TaxRate;
import com.salesmanager.tax.support.ValidationException;

@Component
public class PersistableTaxRateMapper {

	private final ReferenceServiceClient referenceServiceClient;
	private final TaxClassService taxClassService;

	public PersistableTaxRateMapper(
			ReferenceServiceClient referenceServiceClient,
			TaxClassService taxClassService) {
		this.referenceServiceClient = referenceServiceClient;
		this.taxClassService = taxClassService;
	}

	public TaxRate convert(PersistableTaxRate source, MerchantStore store, Language language) {
		TaxRate rate = new TaxRate();
		return merge(source, rate, store, language);
	}

	public TaxRate merge(PersistableTaxRate source, TaxRate destination, MerchantStore store, Language language) {
		Validate.notNull(destination, "destination TaxRate cannot be null");
		Validate.notNull(source, "source TaxRate cannot be null");
		try {
			destination.setId(source.getId());
			destination.setCode(source.getCode());
			destination.setTaxPriority(source.getPriority());

			String langCode = language != null ? language.getCode() : null;
			ReadableCountry readableCountry = referenceServiceClient.getCountryByCode(source.getCountry(), langCode);
			if (readableCountry == null) {
				throw new ValidationException("Invalid country code [" + source.getCountry() + "]");
			}
			Country country = new Country();
			if (readableCountry.getId() != null) {
				country.setId(readableCountry.getId().intValue());
			}
			country.setIsoCode(readableCountry.getCode());
			destination.setCountry(country);

			ReadableZone readableZone = referenceServiceClient.getZoneByCode(
					source.getCountry(), source.getZone(), langCode);
			if (readableZone == null) {
				throw new ValidationException("Invalid zone code [" + source.getZone()
						+ "] for country [" + source.getCountry() + "]");
			}
			Zone zone = new Zone();
			zone.setId(readableZone.getId());
			zone.setCode(readableZone.getCode());
			destination.setZone(zone);
			destination.setStateProvince(source.getZone());

			destination.setMerchantStore(store);
			destination.setTaxClass(taxClassService.getByCode(source.getTaxClass(), store));
			destination.setTaxRate(source.getRate());
			applyDescriptions(destination, source);
			return destination;
		} catch (ValidationException e) {
			throw e;
		} catch (Exception e) {
			throw new ValidationException("An error occurred while creating tax rate", e);
		}
	}

	private void applyDescriptions(TaxRate destination, PersistableTaxRate source) throws Exception {
		if (CollectionUtils.isEmpty(source.getDescriptions())) {
			return;
		}
		for (TaxRateDescription desc : source.getDescriptions()) {
			com.salesmanager.core.model.tax.taxrate.TaxRateDescription description = null;
			if (!CollectionUtils.isEmpty(destination.getDescriptions())) {
				for (com.salesmanager.core.model.tax.taxrate.TaxRateDescription d : destination.getDescriptions()) {
					if (!StringUtils.isBlank(desc.getLanguage())
							&& d.getLanguage() != null
							&& desc.getLanguage().equals(d.getLanguage().getCode())) {
						d.setDescription(desc.getDescription());
						d.setName(desc.getName());
						d.setTitle(desc.getTitle());
						description = d;
						break;
					}
				}
			}
			if (description == null) {
				description = toDescription(desc);
				description.setTaxRate(destination);
				destination.getDescriptions().add(description);
			}
		}
	}

	private com.salesmanager.core.model.tax.taxrate.TaxRateDescription toDescription(TaxRateDescription source)
			throws Exception {
		Validate.notNull(source.getLanguage(), "description.language should not be null");
		com.salesmanager.core.model.tax.taxrate.TaxRateDescription desc =
				new com.salesmanager.core.model.tax.taxrate.TaxRateDescription();
		desc.setId(null);
		desc.setDescription(source.getDescription());
		desc.setName(source.getName());
		if (source.getId() != null && source.getId().longValue() > 0) {
			desc.setId(source.getId());
		}
		ReadableLanguage readableLanguage = referenceServiceClient.getLanguageByCode(source.getLanguage());
		if (readableLanguage == null) {
			throw new ValidationException("Invalid language code [" + source.getLanguage() + "]");
		}
		Language lang = new Language();
		lang.setId(readableLanguage.getId());
		lang.setCode(readableLanguage.getCode());
		desc.setLanguage(lang);
		return desc;
	}
}

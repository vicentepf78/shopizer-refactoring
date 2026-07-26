package com.salesmanager.merchant.populator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.content.ReadableImage;
import com.salesmanager.contracts.common.ReadableAudit;
import com.salesmanager.contracts.merchant.ReadableMerchantStore;
import com.salesmanager.contracts.reference.MeasureUnit;
import com.salesmanager.contracts.reference.ReadableAddress;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.contracts.reference.WeightUnit;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.merchant.support.DateUtil;
import com.salesmanager.merchant.util.MerchantLogoPath;

@Component
public class ReadableMerchantStorePopulator {

	private final ReferenceServiceClient referenceServiceClient;
	private final MerchantLogoPath merchantLogoPath;

	public ReadableMerchantStorePopulator(
			ReferenceServiceClient referenceServiceClient,
			@Qualifier("img") MerchantLogoPath merchantLogoPath) {
		this.referenceServiceClient = referenceServiceClient;
		this.merchantLogoPath = merchantLogoPath;
	}

	public ReadableMerchantStore populate(
			MerchantStore source,
			ReadableMerchantStore target,
			MerchantStore store,
			Language language) throws ConversionException {
		Validate.notNull(source, "MerchantStore must not be null");

		if (target == null) {
			target = new ReadableMerchantStore();
		}

		target.setId(source.getId());
		target.setCode(source.getCode());
		if (source.getDefaultLanguage() != null) {
			target.setDefaultLanguage(source.getDefaultLanguage().getCode());
		}
		if (source.getCurrency() != null) {
			target.setCurrency(source.getCurrency().getCode());
		}
		target.setPhone(source.getStorephone());

		ReadableAddress address = new ReadableAddress();
		address.setAddress(source.getStoreaddress());
		address.setCity(source.getStorecity());
		if (source.getCountry() != null) {
			String langCode = language != null ? language.getCode() : null;
			address.setCountry(source.getCountry().getIsoCode());
			ReadableCountry country = referenceServiceClient.getCountryByCode(source.getCountry().getIsoCode(), langCode);
			if (country != null) {
				address.setCountry(country.getCode());
			}
		}

		if (source.getParent() != null) {
			ReadableMerchantStore parent = populate(source.getParent(), new ReadableMerchantStore(), source, language);
			target.setParent(parent);
		}

		if (target.getParent() == null) {
			target.setRetailer(true);
		} else {
			target.setRetailer(source.isRetailer() != null && source.isRetailer().booleanValue());
		}

		target.setDimension(MeasureUnit.valueOf(source.getSeizeunitcode()));
		target.setWeight(WeightUnit.valueOf(source.getWeightunitcode()));

		if (source.getZone() != null) {
			address.setStateProvince(source.getZone().getCode());
			String langCode = language != null ? language.getCode() : null;
			String countryCode = source.getCountry() != null ? source.getCountry().getIsoCode() : null;
			ReadableZone zone = referenceServiceClient.getZoneByCode(countryCode, source.getZone().getCode(), langCode);
			if (zone != null) {
				address.setStateProvince(zone.getCode());
			}
		}

		if (!StringUtils.isBlank(source.getStorestateprovince())) {
			address.setStateProvince(source.getStorestateprovince());
		}

		if (!StringUtils.isBlank(source.getStoreLogo())) {
			ReadableImage image = new ReadableImage();
			image.setName(source.getStoreLogo());
			image.setPath(merchantLogoPath.buildStoreLogoFilePath(source));
			target.setLogo(image);
		}

		address.setPostalCode(source.getStorepostalcode());
		target.setAddress(address);
		target.setCurrencyFormatNational(source.isCurrencyFormatNational());
		target.setEmail(source.getStoreEmailAddress());
		target.setName(source.getStorename());
		target.setInBusinessSince(DateUtil.formatDate(source.getInBusinessSince()));
		target.setUseCache(source.isUseCache());

		if (!CollectionUtils.isEmpty(source.getLanguages())) {
			List<ReadableLanguage> supported = new ArrayList<>();
			for (Language lang : source.getLanguages()) {
				ReadableLanguage readable = referenceServiceClient.getLanguageByCode(lang.getCode());
				if (readable != null) {
					supported.add(readable);
				} else {
					ReadableLanguage fallback = new ReadableLanguage();
					fallback.setId(lang.getId());
					fallback.setCode(lang.getCode());
					supported.add(fallback);
				}
			}
			target.setSupportedLanguages(supported);
		}

		if (source.getAuditSection() != null) {
			ReadableAudit audit = new ReadableAudit();
			if (source.getAuditSection().getDateCreated() != null) {
				audit.setCreated(DateUtil.formatDate(source.getAuditSection().getDateCreated()));
			}
			if (source.getAuditSection().getDateModified() != null) {
				audit.setModified(DateUtil.formatDate(source.getAuditSection().getDateModified()));
			}
			audit.setUser(source.getAuditSection().getModifiedBy());
			target.setReadableAudit(audit);
		}

		return target;
	}
}

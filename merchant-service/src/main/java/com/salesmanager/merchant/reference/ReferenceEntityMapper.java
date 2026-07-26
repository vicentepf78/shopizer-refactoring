package com.salesmanager.merchant.reference;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.reference.country.CountryRepository;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.merchant.support.ConversionRuntimeException;

@Component
public class ReferenceEntityMapper {

	private final ReferenceServiceClient referenceServiceClient;
	private final LanguageService languageService;
	private final CountryRepository countryRepository;

	public ReferenceEntityMapper(
			ReferenceServiceClient referenceServiceClient,
			LanguageService languageService,
			CountryRepository countryRepository) {
		this.referenceServiceClient = referenceServiceClient;
		this.languageService = languageService;
		this.countryRepository = countryRepository;
	}

	public Country toCountry(String isoCode, String langCode) {
		if (StringUtils.isBlank(isoCode)) {
			return null;
		}
		ReadableCountry readable = referenceServiceClient.getCountryByCode(isoCode, langCode);
		if (readable == null) {
			throw new ConversionRuntimeException("Country [" + isoCode + "] not found");
		}
		Country country = countryRepository.findByIsoCode(readable.getCode());
		if (country == null) {
			throw new ConversionRuntimeException("Country [" + isoCode + "] not found in database");
		}
		return country;
	}

	public Zone toZone(String countryCode, String zoneCode, String langCode) {
		if (StringUtils.isBlank(zoneCode)) {
			return null;
		}
		referenceServiceClient.getZoneByCode(countryCode, zoneCode, langCode);
		Zone zone = new Zone();
		zone.setCode(zoneCode);
		if (StringUtils.isNotBlank(countryCode)) {
			Country country = countryRepository.findByIsoCode(countryCode);
			if (country != null) {
				zone.setCountry(country);
			}
		}
		return zone;
	}

	public Language toLanguage(String code) {
		if (StringUtils.isBlank(code)) {
			return null;
		}
		ReadableLanguage readable = referenceServiceClient.getLanguageByCode(code);
		if (readable == null) {
			throw new ConversionRuntimeException("Language [" + code + "] not found");
		}
		try {
			Language language = languageService.getByCode(code);
			if (language != null) {
				return language;
			}
		} catch (ServiceException e) {
			throw new ConversionRuntimeException(e);
		}
		throw new ConversionRuntimeException("Language [" + code + "] not found in database");
	}
}

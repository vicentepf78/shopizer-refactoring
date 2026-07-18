package com.salesmanager.contracts.client;

import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.contracts.reference.ReadableZone;

public interface ReferenceServiceClient {

	ReadableCountry getCountryByCode(String isoCode, String langCode);

	ReadableZone getZoneByCode(String countryCode, String zoneCode, String langCode);

	ReadableLanguage getLanguageByCode(String code);

}

package com.salesmanager.reference.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.contracts.reference.MeasureUnit;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableCurrency;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.contracts.reference.SizeReferences;
import com.salesmanager.contracts.reference.WeightUnit;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.reference.facade.CountryFacade;
import com.salesmanager.reference.facade.CurrencyFacade;
import com.salesmanager.reference.facade.LanguageFacade;
import com.salesmanager.reference.facade.ZoneFacade;
import com.salesmanager.reference.support.LanguageResolver;

/**
 * Public P1 reference endpoints — path parity with monolito {@code ReferencesApi}.
 * No JWT.
 */
@RestController
@RequestMapping("/api/v1")
public class ReferencesController {

	private final LanguageFacade languageFacade;
	private final CountryFacade countryFacade;
	private final ZoneFacade zoneFacade;
	private final CurrencyFacade currencyFacade;
	private final LanguageResolver languageResolver;

	public ReferencesController(
			LanguageFacade languageFacade,
			CountryFacade countryFacade,
			ZoneFacade zoneFacade,
			CurrencyFacade currencyFacade,
			LanguageResolver languageResolver) {
		this.languageFacade = languageFacade;
		this.countryFacade = countryFacade;
		this.zoneFacade = zoneFacade;
		this.currencyFacade = currencyFacade;
		this.languageResolver = languageResolver;
	}

	@GetMapping("/languages")
	public List<ReadableLanguage> getLanguages() {
		return languageFacade.getLanguages();
	}

	@GetMapping("/country")
	public List<ReadableCountry> getCountry(
			@RequestParam(value = "lang", required = false) String lang,
			@RequestParam(value = "store", required = false) String store) {
		Language language = languageResolver.resolve(lang);
		return countryFacade.getListCountryZones(language);
	}

	@GetMapping("/zones")
	public List<ReadableZone> getZones(
			@RequestParam("code") String code,
			@RequestParam(value = "lang", required = false) String lang,
			@RequestParam(value = "store", required = false) String store) {
		Language language = languageResolver.resolve(lang);
		return zoneFacade.getZones(code, language);
	}

	@GetMapping("/currency")
	public List<ReadableCurrency> getCurrency() {
		return currencyFacade.getList();
	}

	@GetMapping("/measures")
	public SizeReferences measures() {
		SizeReferences sizeReferences = new SizeReferences();
		sizeReferences.setMeasures(Arrays.asList(MeasureUnit.values()));
		sizeReferences.setWeights(Arrays.asList(WeightUnit.values()));
		return sizeReferences;
	}
}

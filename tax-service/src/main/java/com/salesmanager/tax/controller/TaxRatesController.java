package com.salesmanager.tax.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.contracts.common.Entity;
import com.salesmanager.contracts.common.EntityExists;
import com.salesmanager.contracts.common.ReadableEntityList;
import com.salesmanager.contracts.tax.PersistableTaxRate;
import com.salesmanager.contracts.tax.ReadableTaxRate;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.tax.facade.TaxFacade;

@RestController
@RequestMapping("/api/v1")
public class TaxRatesController {

	private final TaxFacade taxFacade;

	public TaxRatesController(TaxFacade taxFacade) {
		this.taxFacade = taxFacade;
	}

	@PostMapping("/private/tax/rate")
	public Entity create(MerchantStore merchantStore, Language language,
			@Valid @RequestBody PersistableTaxRate taxRate) {
		return taxFacade.createTaxRate(taxRate, merchantStore, language);
	}

	@GetMapping(value = "/private/tax/rate/unique", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityExists> exists(
			@RequestParam String code, MerchantStore merchantStore, Language language) {
		boolean exists = taxFacade.existsTaxRate(code, merchantStore, language);
		return new ResponseEntity<>(new EntityExists(exists), HttpStatus.OK);
	}

	@PutMapping("/private/tax/rate/{id}")
	public void update(
			MerchantStore merchantStore,
			@PathVariable Long id,
			Language language,
			@Valid @RequestBody PersistableTaxRate taxRate) {
		taxRate.setId(id);
		taxFacade.updateTaxRate(id, taxRate, merchantStore, language);
	}

	@GetMapping(value = "/private/tax/rates", produces = MediaType.APPLICATION_JSON_VALUE)
	public ReadableEntityList<ReadableTaxRate> list(
			@RequestParam(name = "count", defaultValue = "10") int count,
			@RequestParam(name = "page", defaultValue = "0") int page,
			MerchantStore merchantStore,
			Language language) {
		return taxFacade.taxRates(merchantStore, language);
	}

	@GetMapping("/private/tax/rate/{id}")
	public ReadableTaxRate get(MerchantStore merchantStore, @PathVariable Long id, Language language) {
		return taxFacade.taxRate(id, merchantStore, language);
	}

	@DeleteMapping(value = "/private/tax/rate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public void delete(@PathVariable Long id, MerchantStore merchantStore, Language language) {
		taxFacade.deleteTaxRate(id, merchantStore, language);
	}
}

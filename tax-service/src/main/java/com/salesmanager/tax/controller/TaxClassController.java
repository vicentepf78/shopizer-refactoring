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
import com.salesmanager.contracts.tax.PersistableTaxClass;
import com.salesmanager.contracts.tax.ReadableTaxClass;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.tax.facade.TaxFacade;

@RestController
@RequestMapping("/api/v1")
public class TaxClassController {

	private final TaxFacade taxFacade;

	public TaxClassController(TaxFacade taxFacade) {
		this.taxFacade = taxFacade;
	}

	@PostMapping("/private/tax/class")
	public Entity create(MerchantStore merchantStore, Language language,
			@Valid @RequestBody PersistableTaxClass taxClass) {
		return taxFacade.createTaxClass(taxClass, merchantStore, language);
	}

	@GetMapping(value = "/private/tax/class/unique", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityExists> exists(
			@RequestParam String code, MerchantStore merchantStore, Language language) {
		boolean exists = taxFacade.existsTaxClass(code, merchantStore, language);
		return new ResponseEntity<>(new EntityExists(exists), HttpStatus.OK);
	}

	@PutMapping("/private/tax/class/{id}")
	public void update(
			MerchantStore merchantStore,
			@PathVariable Long id,
			Language language,
			@Valid @RequestBody PersistableTaxClass taxClass) {
		taxClass.setId(id);
		taxFacade.updateTaxClass(id, taxClass, merchantStore, language);
	}

	@GetMapping(value = "/private/tax/class", produces = MediaType.APPLICATION_JSON_VALUE)
	public ReadableEntityList<ReadableTaxClass> list(
			@RequestParam(name = "count", defaultValue = "10") int count,
			@RequestParam(name = "page", defaultValue = "0") int page,
			MerchantStore merchantStore,
			Language language) {
		return taxFacade.taxClasses(merchantStore, language);
	}

	@GetMapping("/private/tax/class/{code}")
	public ReadableTaxClass get(MerchantStore merchantStore, @PathVariable String code, Language language) {
		return taxFacade.taxClass(code, merchantStore, language);
	}

	@DeleteMapping(value = "/private/tax/class/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public void delete(@PathVariable Long id, MerchantStore merchantStore, Language language) {
		taxFacade.deleteTaxClass(id, merchantStore, language);
	}
}

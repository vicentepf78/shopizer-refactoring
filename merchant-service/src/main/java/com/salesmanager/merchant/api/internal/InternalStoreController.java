package com.salesmanager.merchant.api.internal;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.merchant.facade.StoreFacade;

@RestController
@RequestMapping("/internal/v1")
public class InternalStoreController {

	private final StoreFacade storeFacade;

	public InternalStoreController(StoreFacade storeFacade) {
		this.storeFacade = storeFacade;
	}

	@GetMapping(value = "/store/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MerchantStoreSnapshot getSnapshot(@PathVariable String code, Language language) {
		return storeFacade.getSnapshot(code, language);
	}
}

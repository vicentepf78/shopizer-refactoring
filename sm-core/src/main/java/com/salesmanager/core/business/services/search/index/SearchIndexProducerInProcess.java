package com.salesmanager.core.business.services.search.index;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.search.SearchService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.description.ProductDescription;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

@Service
@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "false", matchIfMissing = true)
public class SearchIndexProducerInProcess implements SearchIndexProducer {

	@Autowired
	private SearchService searchService;

	@Override
	public void index(MerchantStore store, Product product) {
		try {
			searchService.index(store, product);
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deleteDocument(MerchantStore store, Long productId, List<String> languages) {
		try {
			searchService.deleteDocument(store, productStub(productId, store, languages));
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	}

	private Product productStub(Long productId, MerchantStore store, List<String> languages) {
		Product product = new Product();
		product.setId(productId);
		product.setMerchantStore(store);
		Set<ProductDescription> descriptions = languages.stream().map(code -> {
			ProductDescription description = new ProductDescription();
			Language language = new Language();
			language.setCode(code);
			description.setLanguage(language);
			return description;
		}).collect(Collectors.toCollection(HashSet::new));
		product.setDescriptions(descriptions);
		return product;
	}

}

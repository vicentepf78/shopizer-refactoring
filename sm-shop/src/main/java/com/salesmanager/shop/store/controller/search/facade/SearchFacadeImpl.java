package com.salesmanager.shop.store.controller.search.facade;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.search.SearchItem;
import com.salesmanager.contracts.search.SearchProductRequest;
import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.category.CategoryService;
import com.salesmanager.core.business.services.catalog.pricing.PricingService;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.search.SearchService;
import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.catalog.category.ReadableCategory;
import com.salesmanager.shop.model.catalog.product.ReadableProduct;
import com.salesmanager.shop.model.entity.ValueList;
import com.salesmanager.shop.populator.catalog.ReadableCategoryPopulator;
import com.salesmanager.shop.populator.catalog.ReadableProductPopulator;
import com.salesmanager.shop.store.api.exception.ConversionRuntimeException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.tenant.TenantEntityBridge;
import com.salesmanager.shop.utils.ImageFilePath;

import modules.commons.search.request.Aggregation;
import modules.commons.search.request.SearchRequest;
import modules.commons.search.request.SearchResponse;

@Service("searchFacade")
@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "false", matchIfMissing = true)
public class SearchFacadeImpl implements SearchFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchFacadeImpl.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Inject
	private SearchService searchService;

	@Inject
	private ProductService productService;

	@Inject
	private CategoryService categoryService;

	@Inject
	private PricingService pricingService;

	@Inject
	@Qualifier("img")
	private ImageFilePath imageUtils;

	@Inject
	private TenantEntityBridge tenantEntityBridge;

	private final static String CATEGORY_FACET_NAME = "categories";
	private final static String MANUFACTURER_FACET_NAME = "manufacturer";
	private final static int AUTOCOMPLETE_ENTRIES_COUNT = 15;

	/**
	 * Index all products from the catalogue Better stop the system, remove ES
	 * indexex manually restart ES and run this query
	 */
	@Override
	@Async
	public void indexAllData(MerchantStoreId storeId) throws Exception {
		MerchantStore store = tenantEntityBridge.resolveStore(storeId);
		List<Product> products = productService.listByStore(store);

		products.stream().forEach(p -> {
			try {
				searchService.index(store, p);
			} catch (ServiceException e) {
				throw new RuntimeException("Exception while indexing products", e);
			}
		});

	}

	@Override
	public List<SearchItem> search(MerchantStoreId storeId, LanguageCode languageCode, SearchProductRequest searchRequest) {
		MerchantStore store;
		Language language;
		try {
			store = tenantEntityBridge.resolveStore(storeId);
			language = tenantEntityBridge.resolveLanguage(languageCode);
		} catch (ConversionException e) {
			throw new ConversionRuntimeException(e);
		}
		SearchResponse response = search(store, language.getCode(), searchRequest.getQuery(), searchRequest.getCount(),
				searchRequest.getStart());
		return response.getItems().stream().map(SearchFacadeImpl::toContractItem).collect(Collectors.toList());
	}

	private SearchResponse search(MerchantStore store, String languageCode, String query, Integer count,
			Integer start) {
		
		Validate.notNull(query,"Search Keyword must not be null");
		Validate.notNull(languageCode, "Language cannot be null");
		Validate.notNull(store,"MerchantStore cannot be null");
		
		
		try {
			LOGGER.debug("Search " + query);
			SearchRequest searchRequest = new SearchRequest();
			searchRequest.setLanguage(languageCode);
			searchRequest.setSearchString(query);
			searchRequest.setStore(store.getCode().toLowerCase());
			
			
			//aggregations
			
			//TODO add scroll
			return searchService.search(store, languageCode, searchRequest, count, start);

		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}

	private List<ReadableCategory> getCategoryFacets(MerchantStore merchantStore, Language language, List<Aggregation> facets) {
		
		
		/**
		List<SearchFacet> categoriesFacets = facets.entrySet().stream()
				.filter(e -> CATEGORY_FACET_NAME.equals(e.getKey())).findFirst().map(Entry::getValue)
				.orElse(Collections.emptyList());

		if (CollectionUtils.isNotEmpty(categoriesFacets)) {

			List<String> categoryCodes = categoriesFacets.stream().map(SearchFacet::getName)
					.collect(Collectors.toList());

			Map<String, Long> productCategoryCount = categoriesFacets.stream()
					.collect(Collectors.toMap(SearchFacet::getKey, SearchFacet::getCount));

			List<Category> categories = categoryService.listByCodes(merchantStore, categoryCodes, language);
			return categories.stream().map(category -> convertCategoryToReadableCategory(merchantStore, language,
					productCategoryCount, category)).collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
		**/
		
		return null;
	}

	private ReadableCategory convertCategoryToReadableCategory(MerchantStore merchantStore, Language language,
			Map<String, Long> productCategoryCount, Category category) {
		ReadableCategoryPopulator populator = new ReadableCategoryPopulator();
		try {
			ReadableCategory categoryProxy = populator.populate(category, new ReadableCategory(), merchantStore,
					language);
			Long total = productCategoryCount.get(categoryProxy.getCode());
			if (total != null) {
				categoryProxy.setProductCount(total.intValue());
			}
			return categoryProxy;
		} catch (ConversionException e) {
			throw new ConversionRuntimeException(e);
		}
	}

	private ReadableProduct convertProductToReadableProduct(Product product, MerchantStore merchantStore,
			Language language) {

		ReadableProductPopulator populator = new ReadableProductPopulator();
		populator.setPricingService(pricingService);
		populator.setimageUtils(imageUtils);

		try {
			return populator.populate(product, new ReadableProduct(), merchantStore, language);
		} catch (ConversionException e) {
			throw new ConversionRuntimeException(e);
		}
	}

	@Override
	public ValueList autocompleteRequest(String word, MerchantStoreId storeId, LanguageCode languageCode) {
		MerchantStore store;
		Language language;
		try {
			store = tenantEntityBridge.resolveStore(storeId);
			language = tenantEntityBridge.resolveLanguage(languageCode);
		} catch (ConversionException e) {
			throw new ConversionRuntimeException(e);
		}
		Validate.notNull(word,"Search Keyword must not be null");
		Validate.notNull(language, "Language cannot be null");
		Validate.notNull(store,"MerchantStore cannot be null");
		
		SearchRequest req = new SearchRequest();
		req.setLanguage(language.getCode());
		req.setStore(store.getCode().toLowerCase());
		req.setSearchString(word);
		req.setLanguage(language.getCode());
		
		SearchResponse response;
		try {
			response = searchService.searchKeywords(store, language.getCode(), req, AUTOCOMPLETE_ENTRIES_COUNT);
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	
		
		List<String> keywords = response.getItems().stream().map(i -> i.getSuggestions()).collect(Collectors.toList());
		
		ValueList valueList = new ValueList();
		valueList.setValues(keywords);
		
		return valueList;
		

	}

	// ponytail: Jackson convertValue — commons SearchItem shares field names with contract DTO
	private static SearchItem toContractItem(modules.commons.search.request.SearchItem source) {
		return MAPPER.convertValue(source, SearchItem.class);
	}


}

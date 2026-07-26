package com.salesmanager.shop.store.controller.search.facade;

import java.util.List;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.shop.model.catalog.SearchProductRequest;
import com.salesmanager.shop.model.entity.ValueList;

import modules.commons.search.request.SearchItem;

/**
 * Different services for searching and indexing data
 * @author c.samson
 *
 */
public interface SearchFacade {
	

	/**
	 * This utility method will re-index all products in the catalogue
	 * @param storeId
	 * @throws Exception
	 */
	public void indexAllData(MerchantStoreId storeId) throws Exception;
	
	/**
	 * Produces a search request against elastic search
	 * @param searchRequest
	 * @return
	 * @throws Exception
	 */
	List<SearchItem> search(MerchantStoreId storeId, LanguageCode language, SearchProductRequest searchRequest);

	/**
	 * List of keywords / autocompletes for a given word being typed
	 * @param query
	 * @param storeId
	 * @param language
	 * @return
	 * @throws Exception
	 */
	ValueList autocompleteRequest(String query, MerchantStoreId storeId, LanguageCode language);
}

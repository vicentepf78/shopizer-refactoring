package com.salesmanager.core.business.services.search.index;

import java.util.List;

import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.merchant.MerchantStore;

public interface SearchIndexProducer {

	void index(MerchantStore store, Product product);

	void deleteDocument(MerchantStore store, Long productId, List<String> languages);

}

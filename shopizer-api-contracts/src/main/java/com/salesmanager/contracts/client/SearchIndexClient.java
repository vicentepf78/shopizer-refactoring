package com.salesmanager.contracts.client;

import java.util.List;

import com.salesmanager.contracts.search.ProductIndexBulkPayload;
import com.salesmanager.contracts.search.ProductIndexPayload;

public interface SearchIndexClient {

	void index(ProductIndexPayload payload);

	void indexBulk(ProductIndexBulkPayload bulk);

	void deleteDocument(Long productId, String store, List<String> languages);

}

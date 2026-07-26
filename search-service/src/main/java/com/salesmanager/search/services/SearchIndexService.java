package com.salesmanager.search.services;

import java.util.List;

import com.salesmanager.contracts.search.ProductIndexPayload;

public interface SearchIndexService {

	void index(ProductIndexPayload payload);

	void indexBulk(List<ProductIndexPayload> payloads);

	void delete(Long productId, String store, List<String> languages);
}

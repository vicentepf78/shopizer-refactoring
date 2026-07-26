package com.salesmanager.shop.strangler.search;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.salesmanager.contracts.client.SearchIndexClient;
import com.salesmanager.contracts.search.ProductIndexBulkPayload;
import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.core.business.services.search.index.ProductIndexPayloadBuilder;
import com.salesmanager.core.business.services.search.index.SearchIndexProducer;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.merchant.MerchantStore;

@Service
@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
public class SearchIndexProducerHttp implements SearchIndexProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchIndexProducerHttp.class);

	private final SearchIndexClient searchIndexClient;
	private final ProductIndexPayloadBuilder payloadBuilder;

	public SearchIndexProducerHttp(SearchIndexClient searchIndexClient, ProductIndexPayloadBuilder payloadBuilder) {
		this.searchIndexClient = searchIndexClient;
		this.payloadBuilder = payloadBuilder;
	}

	@Override
	public void index(MerchantStore store, Product product) {
		try {
			List<ProductIndexPayload> payloads = payloadBuilder.buildAll(store, product);
			if (payloads.isEmpty()) {
				return;
			}
			List<String> languages = payloads.stream()
					.map(ProductIndexPayload::getLanguage)
					.collect(Collectors.toList());
			searchIndexClient.deleteDocument(product.getId(), store.getCode().toLowerCase(), languages);
			indexBulkBatches(payloads);
		} catch (Exception e) {
			// ponytail: GAP-SRCH-07 — log only; outbox deferred to task_07
			LOGGER.error("HTTP index failed for productId={}: {}", product.getId(), e.getMessage(), e);
		}
	}

	@Override
	public void deleteDocument(MerchantStore store, Long productId, List<String> languages) {
		try {
			searchIndexClient.deleteDocument(productId, store.getCode().toLowerCase(), languages);
		} catch (Exception e) {
			LOGGER.error("HTTP delete index failed for productId={}: {}", productId, e.getMessage(), e);
		}
	}

	private void indexBulkBatches(List<ProductIndexPayload> payloads) {
		for (int offset = 0; offset < payloads.size(); offset += ProductIndexBulkPayload.MAX_BATCH_SIZE) {
			int end = Math.min(offset + ProductIndexBulkPayload.MAX_BATCH_SIZE, payloads.size());
			ProductIndexBulkPayload bulk = new ProductIndexBulkPayload();
			bulk.setPayloads(new ArrayList<>(payloads.subList(offset, end)));
			searchIndexClient.indexBulk(bulk);
		}
	}

}

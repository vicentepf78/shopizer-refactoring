package com.salesmanager.shop.strangler.search;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.search.index.SearchIndexProducer;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.strangler.config.Wave2Properties;

@Service
@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
public class SearchBulkIndexOrchestrator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchBulkIndexOrchestrator.class);

	private final ProductService productService;
	private final SearchIndexProducer searchIndexProducer;
	private final Wave2Properties wave2Properties;

	public SearchBulkIndexOrchestrator(
			ProductService productService,
			SearchIndexProducer searchIndexProducer,
			Wave2Properties wave2Properties) {
		this.productService = productService;
		this.searchIndexProducer = searchIndexProducer;
		this.wave2Properties = wave2Properties;
	}

	public void indexAllData(MerchantStore store) {
		List<Product> products = productService.listByStore(store);
		long delayMs = wave2Properties.getSearch().getIndex().getReindexDelayMs();

		for (Product product : products) {
			searchIndexProducer.index(store, product);
			if (delayMs > 0) {
				try {
					Thread.sleep(delayMs);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					LOGGER.warn("Bulk reindex interrupted for store {}", store.getCode());
					return;
				}
			}
		}
	}

}

package com.salesmanager.shop.strangler.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.search.index.SearchIndexProducer;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.strangler.config.Wave2Properties;

@ExtendWith(MockitoExtension.class)
class SearchBulkIndexOrchestratorTest {

	@Mock
	private ProductService productService;

	@Mock
	private SearchIndexProducer searchIndexProducer;

	private Wave2Properties wave2Properties;
	private SearchBulkIndexOrchestrator orchestrator;
	private MerchantStore store;

	@BeforeEach
	void setUp() {
		wave2Properties = new Wave2Properties();
		orchestrator = new SearchBulkIndexOrchestrator(productService, searchIndexProducer, wave2Properties);
		store = new MerchantStore();
		store.setCode("DEFAULT");
	}

	@Test
	void indexAllData_listsProductsByStoreAndIndexesEach() {
		Product first = new Product();
		first.setId(1L);
		Product second = new Product();
		second.setId(2L);
		when(productService.listByStore(store)).thenReturn(Arrays.asList(first, second));

		orchestrator.indexAllData(store);

		verify(productService).listByStore(store);
		verify(searchIndexProducer).index(store, first);
		verify(searchIndexProducer).index(store, second);
	}

	@Test
	void indexAllData_appliesConfiguredDelayBetweenProducts() {
		wave2Properties.getSearch().getIndex().setReindexDelayMs(50L);
		Product first = new Product();
		first.setId(1L);
		Product second = new Product();
		second.setId(2L);
		when(productService.listByStore(store)).thenReturn(Arrays.asList(first, second));

		long started = System.currentTimeMillis();
		orchestrator.indexAllData(store);
		long elapsed = System.currentTimeMillis() - started;

		assertThat(elapsed).isGreaterThanOrEqualTo(50L);
		ArgumentCaptor<Product> indexed = ArgumentCaptor.forClass(Product.class);
		verify(searchIndexProducer, times(2)).index(org.mockito.ArgumentMatchers.eq(store), indexed.capture());
		List<Product> indexedProducts = indexed.getAllValues();
		assertThat(indexedProducts).extracting(Product::getId).containsExactly(1L, 2L);
	}

}

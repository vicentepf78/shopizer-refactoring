package com.salesmanager.core.business.configuration.events.products.listeners;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.salesmanager.core.business.configuration.events.products.DeleteProductEvent;
import com.salesmanager.core.business.configuration.events.products.SaveProductEvent;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.search.index.SearchIndexProducer;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.description.ProductDescription;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

@ExtendWith(MockitoExtension.class)
class IndexProductEventListenerTest {

	@Mock
	private SearchIndexProducer searchIndexProducer;

	@Mock
	private ProductService productService;

	@InjectMocks
	private IndexProductEventListener listener;

	private MerchantStore store;
	private Product product;

	@BeforeEach
	void setUp() {
		store = new MerchantStore();
		store.setCode("DEFAULT");

		Language en = new Language("en");
		ProductDescription description = new ProductDescription();
		description.setLanguage(en);
		Set<ProductDescription> descriptions = new HashSet<>();
		descriptions.add(description);

		product = new Product();
		product.setId(11L);
		product.setMerchantStore(store);
		product.setDescriptions(descriptions);

		ReflectionTestUtils.setField(listener, "noIndex", false);
	}

	@Test
	void saveProductEvent_usesSearchIndexProducer() throws Exception {
		when(productService.findOne(11L, store)).thenReturn(product);
		listener.onApplicationEvent(new SaveProductEvent(this, product));

		verify(searchIndexProducer).index(store, product);
	}

	@Test
	void deleteProductEvent_usesSearchIndexProducer() {
		listener.onApplicationEvent(new DeleteProductEvent(this, product));

		verify(searchIndexProducer).deleteDocument(eq(store), eq(11L), eq(java.util.Collections.singletonList("en")));
	}

}

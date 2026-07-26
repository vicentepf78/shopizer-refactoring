package com.salesmanager.core.business.services.search.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.search.SearchService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.merchant.MerchantStore;

@ExtendWith(MockitoExtension.class)
class SearchIndexProducerInProcessTest {

	@Mock
	private SearchService searchService;

	@InjectMocks
	private SearchIndexProducerInProcess producer;

	private MerchantStore store;
	private Product product;

	@BeforeEach
	void setUp() {
		store = new MerchantStore();
		store.setCode("DEFAULT");
		product = new Product();
		product.setId(7L);
	}

	@Test
	void index_delegatesToLegacySearchService() throws ServiceException {
		producer.index(store, product);

		verify(searchService).index(store, product);
	}

	@Test
	void deleteDocument_delegatesToLegacySearchService() throws ServiceException {
		producer.deleteDocument(store, 7L, Arrays.asList("en"));

		ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
		verify(searchService).deleteDocument(eq(store), productCaptor.capture());
		Product stub = productCaptor.getValue();
		assertThat(stub.getId()).isEqualTo(7L);
		assertThat(stub.getDescriptions()).extracting(d -> d.getLanguage().getCode()).containsExactly("en");
	}

}

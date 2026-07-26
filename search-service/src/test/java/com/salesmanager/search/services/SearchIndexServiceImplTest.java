package com.salesmanager.search.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.search.support.SearchUnavailableException;

import modules.commons.search.SearchModule;
import modules.commons.search.request.IndexItem;

@ExtendWith(MockitoExtension.class)
class SearchIndexServiceImplTest {

	@Mock
	private SearchModule searchModule;

	private SearchIndexServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new SearchIndexServiceImpl(searchModule);
	}

	@Test
	void indexValidPayloadCallsOpenSearchClient() throws Exception {
		ProductIndexPayload payload = samplePayload();

		service.index(payload);

		verify(searchModule).delete(Collections.singletonList("en"), 42L);
		ArgumentCaptor<IndexItem> captor = ArgumentCaptor.forClass(IndexItem.class);
		verify(searchModule).index(captor.capture());
		IndexItem item = captor.getValue();
		assertThat(item.getId()).isEqualTo(42L);
		assertThat(item.getStore()).isEqualTo("default");
		assertThat(item.getName()).isEqualTo("Test product");
		assertThat(item.isAddToCart()).isTrue();
	}

	@Test
	void indexBulkCallsOpenSearchBulkIndex() throws Exception {
		service.indexBulk(Collections.singletonList(samplePayload()));
		verify(searchModule).index(anyList());
	}

	@Test
	void deleteCallsOpenSearchDelete() throws Exception {
		service.delete(7L, "default", Collections.singletonList("en"));
		verify(searchModule).delete(eq(Collections.singletonList("en")), eq(7L));
	}

	@Test
	void openSearchFailureMapsToSearchUnavailable() throws Exception {
		doThrow(new RuntimeException("connection refused")).when(searchModule).index(any(IndexItem.class));

		assertThatThrownBy(() -> service.index(samplePayload()))
				.isInstanceOf(SearchUnavailableException.class)
				.hasMessageContaining("OpenSearch index failed");
	}

	@Test
	void toIndexItemCopiesInventoryAndAttributes() {
		ProductIndexPayload payload = samplePayload();
		Map<String, String> attrs = new HashMap<>();
		attrs.put("color", "red");
		payload.setAttributes(attrs);
		Map<String, String> inventory = new HashMap<>();
		inventory.put("SKU", "ABC");
		payload.setInventory(Collections.singletonList(inventory));

		IndexItem item = service.toIndexItem(payload);
		assertThat(item.getAttributes()).containsEntry("color", "red");
		assertThat(item.getInventory()).hasSize(1);
	}

	private ProductIndexPayload samplePayload() {
		ProductIndexPayload payload = new ProductIndexPayload();
		payload.setSchemaVersion(1);
		payload.setId(42L);
		payload.setStore("DEFAULT");
		payload.setLanguage("en");
		payload.setName("Test product");
		payload.setAddToCart(true);
		return payload;
	}
}

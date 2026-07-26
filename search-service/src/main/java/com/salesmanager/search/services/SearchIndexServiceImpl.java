package com.salesmanager.search.services;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.search.support.SearchUnavailableException;

import modules.commons.search.SearchModule;
import modules.commons.search.request.IndexItem;

@Service
public class SearchIndexServiceImpl implements SearchIndexService {

	private final SearchModule searchModule;

	@Autowired
	public SearchIndexServiceImpl(@Autowired(required = false) SearchModule searchModule) {
		this.searchModule = searchModule;
	}

	@Override
	public void index(ProductIndexPayload payload) {
		Validate.notNull(payload, "ProductIndexPayload cannot be null");
		Validate.notNull(payload.getId(), "ProductIndexPayload.id cannot be null");
		ensureAvailable();

		try {
			if (StringUtils.isNotBlank(payload.getLanguage())) {
				searchModule.delete(Collections.singletonList(payload.getLanguage()), payload.getId());
			}
			searchModule.index(toIndexItem(payload));
		} catch (SearchUnavailableException e) {
			throw e;
		} catch (Exception e) {
			throw new SearchUnavailableException("OpenSearch index failed", e);
		}
	}

	@Override
	public void indexBulk(List<ProductIndexPayload> payloads) {
		Validate.notNull(payloads, "payloads cannot be null");
		ensureAvailable();

		try {
			List<IndexItem> items = payloads.stream().map(this::toIndexItem).collect(Collectors.toList());
			if (!items.isEmpty()) {
				searchModule.index(items);
			}
		} catch (SearchUnavailableException e) {
			throw e;
		} catch (Exception e) {
			throw new SearchUnavailableException("OpenSearch bulk index failed", e);
		}
	}

	@Override
	public void delete(Long productId, String store, List<String> languages) {
		Validate.notNull(productId, "productId cannot be null");
		ensureAvailable();

		List<String> langs = CollectionUtils.isEmpty(languages) ? Collections.emptyList() : languages;
		try {
			if (langs.isEmpty()) {
				return;
			}
			searchModule.delete(langs, productId);
		} catch (SearchUnavailableException e) {
			throw e;
		} catch (Exception e) {
			throw new SearchUnavailableException("OpenSearch delete failed", e);
		}
	}

	IndexItem toIndexItem(ProductIndexPayload payload) {
		IndexItem item = new IndexItem();
		item.setId(payload.getId());
		item.setStore(StringUtils.lowerCase(payload.getStore()));
		item.setLanguage(payload.getLanguage());
		item.setName(payload.getName());
		item.setDescription(payload.getDescription());
		item.setLink(payload.getLink());
		item.setImage(payload.getImage());
		item.setReviews(payload.getReviews());
		item.setBrand(payload.getBrand());
		item.setCategory(payload.getCategory());
		item.setAttributes(payload.getAttributes());
		item.setVariants(payload.getVariants());
		item.setInventory(payload.getInventory());
		item.setAddToCart(Boolean.TRUE.equals(payload.getAddToCart()));
		return item;
	}

	private void ensureAvailable() {
		if (searchModule == null) {
			throw new SearchUnavailableException("OpenSearch is not configured");
		}
	}

	SearchModule getSearchModule() {
		return searchModule;
	}
}

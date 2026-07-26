package com.salesmanager.core.business.services.search.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.salesmanager.contracts.catalog.ProductSnapshot;
import com.salesmanager.contracts.catalog.ProductSnapshotAttribute;
import com.salesmanager.contracts.catalog.ProductSnapshotInventory;
import com.salesmanager.contracts.catalog.ProductSnapshotVariant;
import com.salesmanager.contracts.search.ProductIndexPayload;

public final class ProductSnapshotIndexMapper {

	public static final int SNAPSHOT_BACKED_SCHEMA_VERSION = 2;
	private static final String VSKU = "VSKU";

	private ProductSnapshotIndexMapper() {
	}

	public static ProductIndexPayload toPayload(ProductSnapshot snapshot) {
		ProductIndexPayload payload = new ProductIndexPayload();
		payload.setSchemaVersion(SNAPSHOT_BACKED_SCHEMA_VERSION);
		payload.setId(snapshot.getProductId());
		payload.setStore(snapshot.getStoreCode());
		payload.setLanguage(snapshot.getLanguage());
		payload.setName(snapshot.getName());
		payload.setDescription(snapshot.getDescription());
		payload.setLink(snapshot.getFriendlyUrl());
		payload.setImage(snapshot.getImageUrl());
		payload.setReviews(snapshot.getReviewAverage());
		payload.setBrand(snapshot.getBrandName());
		payload.setCategory(snapshot.getCategoryName());
		payload.setAttributes(toAttributeMap(snapshot.getAttributes()));
		payload.setVariants(toVariantMaps(snapshot.getVariants()));
		payload.setInventory(toInventoryMaps(snapshot.getInventory()));
		payload.setAddToCart(snapshot.getAddToCart());
		return payload;
	}

	private static Map<String, String> toAttributeMap(List<ProductSnapshotAttribute> attributes) {
		Map<String, String> mapped = new HashMap<>();
		if (attributes == null) {
			return mapped;
		}
		for (ProductSnapshotAttribute attribute : attributes) {
			if (attribute != null && isNotBlank(attribute.getName())) {
				mapped.put(attribute.getName(), attribute.getValue());
			}
		}
		return mapped;
	}

	private static List<Map<String, String>> toVariantMaps(List<ProductSnapshotVariant> variants) {
		List<Map<String, String>> mapped = new ArrayList<>();
		if (variants == null) {
			return mapped;
		}
		for (ProductSnapshotVariant variant : variants) {
			if (variant == null) {
				continue;
			}
			Map<String, String> variantMap = new HashMap<>();
			if (variant.getOptions() != null) {
				variantMap.putAll(variant.getOptions());
			}
			if (isNotBlank(variant.getSku())) {
				variantMap.put(VSKU, variant.getSku());
			}
			mapped.add(variantMap);
		}
		return mapped;
	}

	private static List<Map<String, String>> toInventoryMaps(List<ProductSnapshotInventory> inventory) {
		List<Map<String, String>> mapped = new ArrayList<>();
		if (inventory == null) {
			return mapped;
		}
		for (ProductSnapshotInventory item : inventory) {
			if (item == null) {
				continue;
			}
			Map<String, String> inventoryMap = new HashMap<>();
			inventoryMap.put("SKU", item.getSku());
			inventoryMap.put("QTY", String.valueOf(item.getQuantity()));
			inventoryMap.put("PRICE", item.getPrice());
			if (isNotBlank(item.getDiscountPrice())) {
				inventoryMap.put("DISCOUNT", item.getDiscountPrice());
			}
			mapped.add(inventoryMap);
		}
		return mapped;
	}

	private static boolean isNotBlank(String value) {
		return value != null && !value.trim().isEmpty();
	}

}

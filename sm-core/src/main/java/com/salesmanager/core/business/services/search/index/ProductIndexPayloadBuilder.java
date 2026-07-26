package com.salesmanager.core.business.services.search.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.inventory.ProductInventoryService;
import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.category.CategoryDescription;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.attribute.ProductAttribute;
import com.salesmanager.core.model.catalog.product.attribute.ProductOptionDescription;
import com.salesmanager.core.model.catalog.product.attribute.ProductOptionValueDescription;
import com.salesmanager.core.model.catalog.product.description.ProductDescription;
import com.salesmanager.core.model.catalog.product.image.ProductImage;
import com.salesmanager.core.model.catalog.product.inventory.ProductInventory;
import com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer;
import com.salesmanager.core.model.catalog.product.manufacturer.ManufacturerDescription;
import com.salesmanager.core.model.catalog.product.variant.ProductVariant;
import com.salesmanager.core.model.merchant.MerchantStore;

@Service
public class ProductIndexPayloadBuilder {

	private static final String QTY = "QTY";
	private static final String PRICE = "PRICE";
	private static final String DISCOUNT_PRICE = "DISCOUNT";
	private static final String SKU = "SKU";
	private static final String VSKU = "VSKU";

	@Autowired
	private ProductInventoryService productInventoryService;

	public List<ProductIndexPayload> buildAll(MerchantStore store, Product product) throws ServiceException {
		Validate.notNull(product.getId(), "Product.id cannot be null");

		List<Map<String, String>> variants = null;
		if (!CollectionUtils.isEmpty(product.getVariants())) {
			variants = product.getVariants().stream().map(this::variants).collect(Collectors.toList());
		}

		List<Map<String, String>> itemInventory = buildInventory(product);
		Set<ProductDescription> descriptions = product.getDescriptions();
		List<ProductIndexPayload> payloads = new ArrayList<>(descriptions.size());
		for (ProductDescription description : descriptions) {
			payloads.add(buildPayload(store, description, product, variants, itemInventory));
		}
		return payloads;
	}

	private ProductIndexPayload buildPayload(MerchantStore store, ProductDescription description, Product product,
			List<Map<String, String>> variants, List<Map<String, String>> itemInventory) {

		ProductImage image = null;
		if (!CollectionUtils.isEmpty(product.getImages())) {
			image = product.getImages().stream().filter(ProductImage::isDefaultImage).findFirst()
					.orElse(product.getImages().iterator().next());
		}

		ProductIndexPayload payload = new ProductIndexPayload();
		payload.setSchemaVersion(1);
		payload.setId(product.getId());
		payload.setStore(store.getCode().toLowerCase());
		payload.setLanguage(description.getLanguage().getCode());
		payload.setName(description.getName());
		payload.setDescription(description.getDescription());
		payload.setLink(description.getSeUrl());
		payload.setInventory(new ArrayList<>(itemInventory));

		if (product.getManufacturer() != null) {
			payload.setBrand(manufacturer(product.getManufacturer(), description.getLanguage().getCode()));
		}

		if (!CollectionUtils.isEmpty(product.getCategories())) {
			payload.setCategory(category(product.getCategories().iterator().next(), description.getLanguage().getCode()));
		}

		if (!CollectionUtils.isEmpty(product.getAttributes())) {
			payload.setAttributes(attributes(product, description.getLanguage().getCode()));
		}

		if (image != null) {
			payload.setImage(image.getProductImage());
		}

		if (product.getProductReviewAvg() != null) {
			payload.setReviews(product.getProductReviewAvg().toString());
		}

		if (!CollectionUtils.isEmpty(variants)) {
			payload.setVariants(new ArrayList<>(variants));
		}

		payload.setAddToCart(Boolean.FALSE);
		return payload;
	}

	private List<Map<String, String>> buildInventory(Product product) throws ServiceException {
		List<Map<String, String>> itemInventory = new ArrayList<>();
		itemInventory.add(inventory(product));
		if (!CollectionUtils.isEmpty(product.getVariants())) {
			for (ProductVariant variant : product.getVariants()) {
				itemInventory.add(inventory(variant));
			}
		}
		return itemInventory;
	}

	private Map<String, String> inventory(Product product) throws ServiceException {
		ProductInventory inventory = productInventoryService.inventory(product);
		Map<String, String> inventoryMap = new HashMap<>();
		inventoryMap.put(SKU, product.getSku());
		inventoryMap.put(QTY, String.valueOf(inventory.getQuantity()));
		inventoryMap.put(PRICE, String.valueOf(inventory.getPrice().getStringPrice()));
		if (inventory.getPrice().isDiscounted()) {
			inventoryMap.put(DISCOUNT_PRICE, String.valueOf(inventory.getPrice().getStringDiscountedPrice()));
		}
		return inventoryMap;
	}

	private Map<String, String> inventory(ProductVariant product) throws ServiceException {
		ProductInventory inventory = productInventoryService.inventory(product);
		Map<String, String> inventoryMap = new HashMap<>();
		inventoryMap.put(SKU, product.getSku());
		inventoryMap.put(QTY, String.valueOf(inventory.getQuantity()));
		inventoryMap.put(PRICE, String.valueOf(inventory.getPrice().getStringPrice()));
		if (inventory.getPrice().isDiscounted()) {
			inventoryMap.put(DISCOUNT_PRICE, String.valueOf(inventory.getPrice().getStringDiscountedPrice()));
		}
		return inventoryMap;
	}

	private Map<String, String> variants(ProductVariant variant) {
		if (variant == null) {
			return null;
		}

		Map<String, String> variantMap = new HashMap<>();
		if (variant.getVariation() != null) {
			String variantCode = variant.getVariation().getProductOption().getCode();
			String variantValueCode = variant.getVariation().getProductOptionValue().getCode();
			variantMap.put(variantCode, variantValueCode);
		}

		if (variant.getVariationValue() != null) {
			String variantCode = variant.getVariationValue().getProductOption().getCode();
			String variantValueCode = variant.getVariationValue().getProductOptionValue().getCode();
			variantMap.put(variantCode, variantValueCode);
		}

		if (!StringUtils.isBlank(variant.getSku())) {
			variantMap.put(VSKU, variant.getSku());
		}

		return variantMap;
	}

	private String manufacturer(Manufacturer manufacturer, String language) {
		ManufacturerDescription description = manufacturer.getDescriptions().stream()
				.filter(d -> d.getLanguage().getCode().equals(language)).findFirst().get();
		return description.getName();
	}

	private String category(Category category, String language) {
		CategoryDescription description = category.getDescriptions().stream()
				.filter(d -> d.getLanguage().getCode().equals(language)).findFirst().get();
		return description.getName();
	}

	private Map<String, String> attributes(Product product, String language) {
		Map<String, String> allAttributes = new HashMap<>();
		for (ProductAttribute attribute : product.getAttributes()) {
			allAttributes.putAll(attribute(attribute, language));
		}
		return allAttributes;
	}

	private Map<String, String> attribute(ProductAttribute attribute, String language) {
		Map<String, String> attributeValue = new HashMap<>();
		ProductOptionDescription optionDescription = attribute.getProductOption().getDescriptions().stream()
				.filter(a -> a.getLanguage().getCode().equals(language)).findFirst().get();
		ProductOptionValueDescription value = attribute.getProductOptionValue().getDescriptions().stream()
				.filter(a -> a.getLanguage().getCode().equals(language)).findFirst().get();
		attributeValue.put(optionDescription.getName(), value.getName());
		return attributeValue;
	}

}

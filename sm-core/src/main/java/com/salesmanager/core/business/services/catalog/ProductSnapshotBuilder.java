package com.salesmanager.core.business.services.catalog;

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

import com.salesmanager.contracts.catalog.ProductSnapshot;
import com.salesmanager.contracts.catalog.ProductSnapshotAttribute;
import com.salesmanager.contracts.catalog.ProductSnapshotInventory;
import com.salesmanager.contracts.catalog.ProductSnapshotVariant;
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
public class ProductSnapshotBuilder {

	@Autowired
	private ProductInventoryService productInventoryService;

	public List<ProductSnapshot> buildAll(MerchantStore store, Product product) throws ServiceException {
		Validate.notNull(product.getId(), "Product.id cannot be null");

		List<ProductSnapshotVariant> variants = null;
		if (!CollectionUtils.isEmpty(product.getVariants())) {
			variants = product.getVariants().stream().map(this::variant).collect(Collectors.toList());
		}

		List<ProductSnapshotInventory> itemInventory = buildInventory(product);
		Set<ProductDescription> descriptions = product.getDescriptions();
		List<ProductSnapshot> snapshots = new ArrayList<>(descriptions.size());
		for (ProductDescription description : descriptions) {
			snapshots.add(buildSnapshot(store, description, product, variants, itemInventory));
		}
		return snapshots;
	}

	private ProductSnapshot buildSnapshot(MerchantStore store, ProductDescription description, Product product,
			List<ProductSnapshotVariant> variants, List<ProductSnapshotInventory> itemInventory) {

		ProductImage image = null;
		if (!CollectionUtils.isEmpty(product.getImages())) {
			image = product.getImages().stream().filter(ProductImage::isDefaultImage).findFirst()
					.orElse(product.getImages().iterator().next());
		}

		ProductSnapshot snapshot = new ProductSnapshot();
		snapshot.setSchemaVersion(1);
		snapshot.setProductId(product.getId());
		snapshot.setStoreCode(store.getCode().toLowerCase());
		snapshot.setSku(product.getSku());
		snapshot.setLanguage(description.getLanguage().getCode());
		snapshot.setName(description.getName());
		snapshot.setDescription(description.getDescription());
		snapshot.setFriendlyUrl(description.getSeUrl());
		snapshot.setInventory(new ArrayList<>(itemInventory));

		if (product.getManufacturer() != null) {
			snapshot.setBrandName(manufacturer(product.getManufacturer(), description.getLanguage().getCode()));
		}

		if (!CollectionUtils.isEmpty(product.getCategories())) {
			snapshot.setCategoryName(category(product.getCategories().iterator().next(), description.getLanguage().getCode()));
		}

		if (!CollectionUtils.isEmpty(product.getAttributes())) {
			snapshot.setAttributes(attributes(product, description.getLanguage().getCode()));
		}

		if (image != null) {
			snapshot.setImageUrl(image.getProductImage());
		}

		if (product.getProductReviewAvg() != null) {
			snapshot.setReviewAverage(product.getProductReviewAvg().toString());
		}

		if (!CollectionUtils.isEmpty(variants)) {
			snapshot.setVariants(new ArrayList<>(variants));
		}

		snapshot.setAddToCart(Boolean.FALSE);
		return snapshot;
	}

	private List<ProductSnapshotInventory> buildInventory(Product product) throws ServiceException {
		List<ProductSnapshotInventory> itemInventory = new ArrayList<>();
		itemInventory.add(inventory(product));
		if (!CollectionUtils.isEmpty(product.getVariants())) {
			for (ProductVariant variant : product.getVariants()) {
				itemInventory.add(inventory(variant));
			}
		}
		return itemInventory;
	}

	private ProductSnapshotInventory inventory(Product product) throws ServiceException {
		ProductInventory inventory = productInventoryService.inventory(product);
		ProductSnapshotInventory snapshotInventory = new ProductSnapshotInventory();
		snapshotInventory.setSku(product.getSku());
		snapshotInventory.setQuantity(inventory.getQuantity());
		snapshotInventory.setPrice(String.valueOf(inventory.getPrice().getStringPrice()));
		if (inventory.getPrice().isDiscounted()) {
			snapshotInventory.setDiscountPrice(String.valueOf(inventory.getPrice().getStringDiscountedPrice()));
		}
		return snapshotInventory;
	}

	private ProductSnapshotInventory inventory(ProductVariant product) throws ServiceException {
		ProductInventory inventory = productInventoryService.inventory(product);
		ProductSnapshotInventory snapshotInventory = new ProductSnapshotInventory();
		snapshotInventory.setSku(product.getSku());
		snapshotInventory.setQuantity(inventory.getQuantity());
		snapshotInventory.setPrice(String.valueOf(inventory.getPrice().getStringPrice()));
		if (inventory.getPrice().isDiscounted()) {
			snapshotInventory.setDiscountPrice(String.valueOf(inventory.getPrice().getStringDiscountedPrice()));
		}
		return snapshotInventory;
	}

	private ProductSnapshotVariant variant(ProductVariant variant) {
		if (variant == null) {
			return null;
		}

		ProductSnapshotVariant snapshotVariant = new ProductSnapshotVariant();
		Map<String, String> options = new HashMap<>();
		if (variant.getVariation() != null) {
			String variantCode = variant.getVariation().getProductOption().getCode();
			String variantValueCode = variant.getVariation().getProductOptionValue().getCode();
			options.put(variantCode, variantValueCode);
		}

		if (variant.getVariationValue() != null) {
			String variantCode = variant.getVariationValue().getProductOption().getCode();
			String variantValueCode = variant.getVariationValue().getProductOptionValue().getCode();
			options.put(variantCode, variantValueCode);
		}

		snapshotVariant.setOptions(options);
		if (!StringUtils.isBlank(variant.getSku())) {
			snapshotVariant.setSku(variant.getSku());
		}

		return snapshotVariant;
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

	private List<ProductSnapshotAttribute> attributes(Product product, String language) {
		List<ProductSnapshotAttribute> allAttributes = new ArrayList<>();
		for (ProductAttribute attribute : product.getAttributes()) {
			allAttributes.add(attribute(attribute, language));
		}
		return allAttributes;
	}

	private ProductSnapshotAttribute attribute(ProductAttribute attribute, String language) {
		ProductSnapshotAttribute snapshotAttribute = new ProductSnapshotAttribute();
		ProductOptionDescription optionDescription = attribute.getProductOption().getDescriptions().stream()
				.filter(a -> a.getLanguage().getCode().equals(language)).findFirst().get();
		ProductOptionValueDescription value = attribute.getProductOptionValue().getDescriptions().stream()
				.filter(a -> a.getLanguage().getCode().equals(language)).findFirst().get();
		snapshotAttribute.setName(optionDescription.getName());
		snapshotAttribute.setValue(value.getName());
		return snapshotAttribute;
	}

}

package com.salesmanager.contracts.catalog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSnapshot implements Serializable {

	private static final long serialVersionUID = 1L;

	private int schemaVersion = 1;
	private Long productId;
	private String storeCode;
	private String sku;
	private String language;
	private String name;
	private String description;
	private String friendlyUrl;
	private String imageUrl;
	private String reviewAverage;
	private String brandName;
	private String categoryName;
	private List<ProductSnapshotAttribute> attributes = new ArrayList<>();
	private List<ProductSnapshotVariant> variants = new ArrayList<>();
	private List<ProductSnapshotInventory> inventory = new ArrayList<>();
	private Boolean addToCart = Boolean.FALSE;

	public int getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFriendlyUrl() {
		return friendlyUrl;
	}

	public void setFriendlyUrl(String friendlyUrl) {
		this.friendlyUrl = friendlyUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getReviewAverage() {
		return reviewAverage;
	}

	public void setReviewAverage(String reviewAverage) {
		this.reviewAverage = reviewAverage;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public List<ProductSnapshotAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ProductSnapshotAttribute> attributes) {
		this.attributes = attributes;
	}

	public List<ProductSnapshotVariant> getVariants() {
		return variants;
	}

	public void setVariants(List<ProductSnapshotVariant> variants) {
		this.variants = variants;
	}

	public List<ProductSnapshotInventory> getInventory() {
		return inventory;
	}

	public void setInventory(List<ProductSnapshotInventory> inventory) {
		this.inventory = inventory;
	}

	public Boolean getAddToCart() {
		return addToCart;
	}

	public void setAddToCart(Boolean addToCart) {
		this.addToCart = addToCart;
	}

}

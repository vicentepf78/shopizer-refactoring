package com.salesmanager.contracts.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductIndexPayload implements Serializable {

	private static final long serialVersionUID = 1L;

	private int schemaVersion = 1;
	private Long id;
	private String store;
	private String language;
	private String name;
	private String description;
	private String link;
	private String image;
	private String reviews;
	private String brand;
	private String category;
	private Map<String, String> attributes = new HashMap<>();
	private List<Map<String, String>> variants = new ArrayList<>();
	private List<Map<String, String>> inventory = new ArrayList<>();
	private Boolean addToCart = Boolean.FALSE;

	public int getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		this.store = store;
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

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getReviews() {
		return reviews;
	}

	public void setReviews(String reviews) {
		this.reviews = reviews;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public List<Map<String, String>> getVariants() {
		return variants;
	}

	public void setVariants(List<Map<String, String>> variants) {
		this.variants = variants;
	}

	public List<Map<String, String>> getInventory() {
		return inventory;
	}

	public void setInventory(List<Map<String, String>> inventory) {
		this.inventory = inventory;
	}

	public Boolean getAddToCart() {
		return addToCart;
	}

	public void setAddToCart(Boolean addToCart) {
		this.addToCart = addToCart;
	}

}

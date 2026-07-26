package com.salesmanager.core.business.services.search.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.inventory.ProductInventoryService;
import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.category.CategoryDescription;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.description.ProductDescription;
import com.salesmanager.core.model.catalog.product.inventory.ProductInventory;
import com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer;
import com.salesmanager.core.model.catalog.product.manufacturer.ManufacturerDescription;
import com.salesmanager.core.model.catalog.product.price.FinalPrice;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

@ExtendWith(MockitoExtension.class)
class ProductIndexPayloadBuilderTest {

	@Mock
	private ProductInventoryService productInventoryService;

	@InjectMocks
	private ProductIndexPayloadBuilder builder;

	private MerchantStore store;
	private Product product;

	@BeforeEach
	void setUp() {
		store = new MerchantStore();
		store.setCode("DEFAULT");
		product = sampleProduct();
	}

	@Test
	void buildAll_generatesPayloadPerLanguageWithSchemaVersionOne() throws ServiceException {
		when(productInventoryService.inventory(any(Product.class))).thenReturn(sampleInventory("SKU-1", "19.99"));
		List<ProductIndexPayload> payloads = builder.buildAll(store, product);

		assertThat(payloads).hasSize(2);
		assertThat(payloads).extracting(ProductIndexPayload::getSchemaVersion).containsOnly(1);
		assertThat(payloads).extracting(ProductIndexPayload::getLanguage).containsExactlyInAnyOrder("en", "fr");
		assertThat(payloads).allSatisfy(payload -> {
			assertThat(payload.getId()).isEqualTo(42L);
			assertThat(payload.getStore()).isEqualTo("default");
			assertThat(payload.getName()).isNotBlank();
			assertThat(payload.getInventory()).isNotEmpty();
			assertThat(payload.getAddToCart()).isFalse();
		});

		ProductIndexPayload english = payloads.stream()
				.filter(p -> "en".equals(p.getLanguage()))
				.findFirst()
				.orElseThrow(AssertionError::new);
		assertThat(english.getName()).isEqualTo("Sample product");
		assertThat(english.getBrand()).isEqualTo("Acme");
		assertThat(english.getCategory()).isEqualTo("Books");
		assertThat(english.getLink()).isEqualTo("sample-product");
	}

	private static Product sampleProduct() {
		Language en = new Language("en");
		Language fr = new Language("fr");

		Product product = new Product();
		product.setId(42L);
		product.setSku("SKU-1");

		ProductDescription enDescription = new ProductDescription();
		enDescription.setName("Sample product");
		enDescription.setDescription("English description");
		enDescription.setSeUrl("sample-product");
		enDescription.setLanguage(en);

		ProductDescription frDescription = new ProductDescription();
		frDescription.setName("Produit exemple");
		frDescription.setDescription("Description française");
		frDescription.setSeUrl("produit-exemple");
		frDescription.setLanguage(fr);

		Set<ProductDescription> descriptions = new HashSet<>();
		descriptions.add(enDescription);
		descriptions.add(frDescription);
		product.setDescriptions(descriptions);

		Manufacturer manufacturer = new Manufacturer();
		ManufacturerDescription enManufacturer = new ManufacturerDescription();
		enManufacturer.setName("Acme");
		enManufacturer.setLanguage(en);
		ManufacturerDescription frManufacturer = new ManufacturerDescription();
		frManufacturer.setName("Acme FR");
		frManufacturer.setLanguage(fr);
		Set<ManufacturerDescription> manufacturerDescriptions = new HashSet<>();
		manufacturerDescriptions.add(enManufacturer);
		manufacturerDescriptions.add(frManufacturer);
		manufacturer.setDescriptions(manufacturerDescriptions);
		product.setManufacturer(manufacturer);

		Category category = new Category();
		CategoryDescription enCategory = new CategoryDescription();
		enCategory.setName("Books");
		enCategory.setLanguage(en);
		CategoryDescription frCategory = new CategoryDescription();
		frCategory.setName("Livres");
		frCategory.setLanguage(fr);
		Set<CategoryDescription> categoryDescriptions = new HashSet<>();
		categoryDescriptions.add(enCategory);
		categoryDescriptions.add(frCategory);
		category.setDescriptions(categoryDescriptions);
		Set<Category> categories = new HashSet<>();
		categories.add(category);
		product.setCategories(categories);

		return product;
	}

	@Test
	void buildAll_includesDiscountAndVariantInventory() throws ServiceException {
		when(productInventoryService.inventory(org.mockito.ArgumentMatchers.any(Product.class)))
				.thenReturn(sampleInventory("SKU-1", "19.99", true));
		when(productInventoryService.inventory(org.mockito.ArgumentMatchers.any(
				com.salesmanager.core.model.catalog.product.variant.ProductVariant.class)))
				.thenReturn(sampleInventory("V-SKU", "24.99", false));

		com.salesmanager.core.model.catalog.product.variant.ProductVariant variant =
				new com.salesmanager.core.model.catalog.product.variant.ProductVariant();
		variant.setSku("V-SKU");
		java.util.Set<com.salesmanager.core.model.catalog.product.variant.ProductVariant> variants = new HashSet<>();
		variants.add(variant);
		product.setVariants(variants);

		List<ProductIndexPayload> payloads = builder.buildAll(store, product);

		assertThat(payloads.get(0).getInventory()).hasSize(2);
		assertThat(payloads.get(0).getInventory().get(0)).containsKey("DISCOUNT");
	}

	private static ProductInventory sampleInventory(String sku, String price) {
		return sampleInventory(sku, price, false);
	}

	private static ProductInventory sampleInventory(String sku, String price, boolean discounted) {
		FinalPrice finalPrice = new FinalPrice();
		finalPrice.setOriginalPrice(new BigDecimal(price));
		finalPrice.setFinalPrice(new BigDecimal(price));
		finalPrice.setStringPrice(price);
		finalPrice.setDiscounted(discounted);
		if (discounted) {
			finalPrice.setStringDiscountedPrice("17.99");
		}

		ProductInventory inventory = new ProductInventory();
		inventory.setSku(sku);
		inventory.setQuantity(5L);
		inventory.setPrice(finalPrice);
		return inventory;
	}

}

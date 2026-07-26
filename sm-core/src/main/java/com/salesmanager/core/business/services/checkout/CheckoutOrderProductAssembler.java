package com.salesmanager.core.business.services.checkout;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.attribute.ProductAttributeService;
import com.salesmanager.core.business.services.catalog.product.file.DigitalProductService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.attribute.ProductAttribute;
import com.salesmanager.core.model.catalog.product.file.DigitalProduct;
import com.salesmanager.core.model.catalog.product.price.FinalPrice;
import com.salesmanager.core.model.catalog.product.price.ProductPrice;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.orderproduct.OrderProduct;
import com.salesmanager.core.model.order.orderproduct.OrderProductAttribute;
import com.salesmanager.core.model.order.orderproduct.OrderProductDownload;
import com.salesmanager.core.model.order.orderproduct.OrderProductPrice;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.shoppingcart.ShoppingCartAttributeItem;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;

/**
 * ponytail: mirrors shop {@code OrderProductPopulator} until checkout can depend on a shared assembler module.
 */
final class CheckoutOrderProductAssembler {

	private static final int MAX_DOWNLOAD_DAYS = 30;

	private final ProductService productService;
	private final DigitalProductService digitalProductService;
	private final ProductAttributeService productAttributeService;

	CheckoutOrderProductAssembler(ProductService productService, DigitalProductService digitalProductService,
			ProductAttributeService productAttributeService) {
		this.productService = productService;
		this.digitalProductService = digitalProductService;
		this.productAttributeService = productAttributeService;
	}

	OrderProduct populate(ShoppingCartItem source, OrderProduct target, MerchantStore store, Language language)
			throws ConversionException {
		Validate.notNull(productService, "productService must be set");
		Validate.notNull(digitalProductService, "digitalProductService must be set");
		Validate.notNull(productAttributeService, "productAttributeService must be set");

		try {
			Product modelProduct = productService.getBySku(source.getSku(), store, language);
			if (modelProduct == null) {
				throw new ConversionException("Cannot get product with sku " + source.getSku());
			}

			if (modelProduct.getMerchantStore().getId().intValue() != store.getId().intValue()) {
				throw new ConversionException("Invalid product with sku " + source.getSku());
			}

			DigitalProduct digitalProduct = digitalProductService.getByProduct(store, modelProduct);

			if (digitalProduct != null) {
				OrderProductDownload orderProductDownload = new OrderProductDownload();
				orderProductDownload.setOrderProductFilename(digitalProduct.getProductFileName());
				orderProductDownload.setOrderProduct(target);
				orderProductDownload.setDownloadCount(0);
				orderProductDownload.setMaxdays(MAX_DOWNLOAD_DAYS);
				target.getDownloads().add(orderProductDownload);
			}

			target.setOneTimeCharge(source.getItemPrice());
			target.setProductName(source.getProduct().getDescriptions().iterator().next().getName());
			target.setProductQuantity(source.getQuantity());
			target.setSku(source.getProduct().getSku());

			FinalPrice finalPrice = source.getFinalPrice();
			if (finalPrice == null) {
				throw new ConversionException("Object final price not populated in shoppingCartItem (source)");
			}

			OrderProductPrice orderProductPrice = orderProductPrice(finalPrice);
			orderProductPrice.setOrderProduct(target);

			Set<OrderProductPrice> prices = new HashSet<OrderProductPrice>();
			prices.add(orderProductPrice);

			List<FinalPrice> otherPrices = finalPrice.getAdditionalPrices();
			if (otherPrices != null) {
				for (FinalPrice otherPrice : otherPrices) {
					OrderProductPrice other = orderProductPrice(otherPrice);
					other.setOrderProduct(target);
					prices.add(other);
				}
			}

			target.setPrices(prices);

			Set<ShoppingCartAttributeItem> attributeItems = source.getAttributes();
			if (!CollectionUtils.isEmpty(attributeItems)) {
				Set<OrderProductAttribute> attributes = new HashSet<OrderProductAttribute>();
				for (ShoppingCartAttributeItem attribute : attributeItems) {
					OrderProductAttribute orderProductAttribute = new OrderProductAttribute();
					orderProductAttribute.setOrderProduct(target);
					Long id = attribute.getProductAttributeId();
					ProductAttribute attr = productAttributeService.getById(id);
					if (attr == null) {
						throw new ConversionException("Attribute id " + id + " does not exists");
					}

					if (attr.getProduct().getMerchantStore().getId().intValue() != store.getId().intValue()) {
						throw new ConversionException("Attribute id " + id + " invalid for this store");
					}

					orderProductAttribute.setProductAttributeIsFree(attr.getProductAttributeIsFree());
					orderProductAttribute.setProductAttributeName(
							attr.getProductOption().getDescriptionsSettoList().get(0).getName());
					orderProductAttribute.setProductAttributeValueName(
							attr.getProductOptionValue().getDescriptionsSettoList().get(0).getName());
					orderProductAttribute.setProductAttributePrice(attr.getProductAttributePrice());
					orderProductAttribute.setProductAttributeWeight(attr.getProductAttributeWeight());
					orderProductAttribute.setProductOptionId(attr.getProductOption().getId());
					orderProductAttribute.setProductOptionValueId(attr.getProductOptionValue().getId());
					attributes.add(orderProductAttribute);
				}
				target.setOrderAttributes(attributes);
			}

		} catch (ConversionException e) {
			throw e;
		} catch (Exception e) {
			throw new ConversionException(e);
		}

		return target;
	}

	private static OrderProductPrice orderProductPrice(FinalPrice price) {
		OrderProductPrice orderProductPrice = new OrderProductPrice();

		ProductPrice productPrice = price.getProductPrice();

		orderProductPrice.setDefaultPrice(productPrice.isDefaultPrice());
		orderProductPrice.setProductPrice(price.getFinalPrice());
		orderProductPrice.setProductPriceCode(productPrice.getCode());
		if (productPrice.getDescriptions() != null && productPrice.getDescriptions().size() > 0) {
			orderProductPrice.setProductPriceName(productPrice.getDescriptions().iterator().next().getName());
		}
		if (price.isDiscounted()) {
			orderProductPrice.setProductPriceSpecial(productPrice.getProductPriceSpecialAmount());
			orderProductPrice.setProductPriceSpecialStartDate(productPrice.getProductPriceSpecialStartDate());
			orderProductPrice.setProductPriceSpecialEndDate(productPrice.getProductPriceSpecialEndDate());
		}

		return orderProductPrice;
	}

}

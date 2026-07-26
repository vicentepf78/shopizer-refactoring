package com.salesmanager.core.modules.integration.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class PaymentLineItemDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long cartItemId;
	private String sku;
	private Integer quantity;
	private BigDecimal itemPrice;
	private Long productId;
	private Long variant;

	public Long getCartItemId() {
		return cartItemId;
	}

	public void setCartItemId(Long cartItemId) {
		this.cartItemId = cartItemId;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getItemPrice() {
		return itemPrice;
	}

	public void setItemPrice(BigDecimal itemPrice) {
		this.itemPrice = itemPrice;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getVariant() {
		return variant;
	}

	public void setVariant(Long variant) {
		this.variant = variant;
	}

}

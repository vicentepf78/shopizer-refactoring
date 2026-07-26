package com.salesmanager.core.business.services.payments;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.core.modules.integration.payment.dto.PaymentLineItemDto;

class IntegrationContextMapperTest {

	@Test
	void toLineItemsMapsCartItemFieldsToPaymentLineItemDto() {
		ShoppingCartItem item = new ShoppingCartItem();
		item.setId(42L);
		item.setSku("SKU-TEST");
		item.setQuantity(3);
		item.setItemPrice(new BigDecimal("19.99"));
		item.setProductId(100L);
		item.setVariant(7L);

		PaymentLineItemDto dto = IntegrationContextMapper.toLineItems(Collections.singletonList(item)).get(0);

		assertThat(dto.getCartItemId()).isEqualTo(42L);
		assertThat(dto.getSku()).isEqualTo("SKU-TEST");
		assertThat(dto.getQuantity()).isEqualTo(3);
		assertThat(dto.getItemPrice()).isEqualByComparingTo("19.99");
		assertThat(dto.getProductId()).isEqualTo(100L);
		assertThat(dto.getVariant()).isEqualTo(7L);
	}

}

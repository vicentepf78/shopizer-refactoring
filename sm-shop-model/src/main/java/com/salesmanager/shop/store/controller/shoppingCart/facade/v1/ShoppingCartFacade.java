package com.salesmanager.shop.store.controller.shoppingCart.facade.v1;

import java.util.Optional;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.shop.model.shoppingcart.ReadableShoppingCart;

public interface ShoppingCartFacade {
	
	ReadableShoppingCart get(Optional<String> cart, Long customerId, MerchantStoreId storeId, LanguageCode language);

}

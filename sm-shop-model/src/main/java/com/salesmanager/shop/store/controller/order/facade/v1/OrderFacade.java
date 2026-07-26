package com.salesmanager.shop.store.controller.order.facade.v1;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.shop.model.order.v1.ReadableOrderConfirmation;

public interface OrderFacade {
	
	ReadableOrderConfirmation orderConfirmation(Order order, Customer customer, MerchantStoreId storeId, LanguageCode language);

}

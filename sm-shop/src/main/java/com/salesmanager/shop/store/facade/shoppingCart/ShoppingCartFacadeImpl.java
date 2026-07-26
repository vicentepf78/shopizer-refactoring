package com.salesmanager.shop.store.facade.shoppingCart;

import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.shoppingcart.ShoppingCartService;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.shoppingcart.ShoppingCart;
import com.salesmanager.shop.model.shoppingcart.ReadableShoppingCart;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.controller.shoppingCart.facade.v1.ShoppingCartFacade;
import com.salesmanager.shop.tenant.TenantEntityBridge;

@Service("shoppingCartFacadev1")
public class ShoppingCartFacadeImpl implements ShoppingCartFacade {

	@Autowired
	private CustomerService customerService;
	
	@Autowired
	private ShoppingCartService shoppingCartService;

	@Autowired
	private com.salesmanager.shop.store.controller.customer.facade.CustomerFacade customerFacade;

	@Autowired
	private com.salesmanager.shop.store.controller.shoppingCart.facade.ShoppingCartFacade shoppingCartFacade; // legacy
																												// facade

	@Autowired
	private TenantEntityBridge tenantEntityBridge;

	@Override
	public ReadableShoppingCart get(Optional<String> cart, Long customerId, MerchantStoreId storeId, LanguageCode languageCode) {

		MerchantStore store;
		Language language;
		try {
			store = tenantEntityBridge.resolveStore(storeId);
			language = tenantEntityBridge.resolveLanguage(languageCode);
		} catch (com.salesmanager.core.business.exception.ConversionException e) {
			throw new ServiceRuntimeException(e);
		}

		Validate.notNull(customerId, "Customer id cannot be null");
		Validate.notNull(store, "MerchantStore cannot be null");
		
		try {

			// lookup customer
			Customer customer = customerService.getById(customerId);

			if (customer == null) {
				throw new ResourceNotFoundException("No Customer found for id [" + customerId + "]");
			}
			
			ShoppingCart cartModel = shoppingCartService.getShoppingCart(customer, store);
			
			if(cart.isPresent()) {
				cartModel = customerFacade.mergeCart(customer, cart.get(), store, language);
			}
			
			if(cartModel == null) {
				return null;
			}
			
		   ReadableShoppingCart readableCart = shoppingCartFacade.readableCart(cartModel, store, language);
		   
		   return readableCart;

		} catch (Exception e) {
			
			throw new ServiceRuntimeException(e);

		}

	}

}

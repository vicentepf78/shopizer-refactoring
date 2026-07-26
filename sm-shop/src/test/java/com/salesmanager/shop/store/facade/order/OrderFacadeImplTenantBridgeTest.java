package com.salesmanager.shop.store.facade.order;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.mapper.customer.ReadableCustomerMapper;
import com.salesmanager.shop.mapper.order.ReadableOrderProductMapper;
import com.salesmanager.shop.mapper.order.ReadableOrderTotalMapper;
import com.salesmanager.shop.model.customer.ReadableCustomer;
import com.salesmanager.shop.model.order.v1.ReadableOrderConfirmation;
import com.salesmanager.shop.tenant.TenantEntityBridge;
import com.salesmanager.shop.utils.LabelUtils;

@ExtendWith(MockitoExtension.class)
class OrderFacadeImplTenantBridgeTest {

	@Mock
	private ReadableCustomerMapper readableCustomerMapper;

	@Mock
	private ReadableOrderTotalMapper readableOrderTotalMapper;

	@Mock
	private ReadableOrderProductMapper readableOrderProductMapper;

	@Mock
	private LabelUtils messages;

	@Mock
	private TenantEntityBridge tenantEntityBridge;

	@InjectMocks
	private OrderFacadeImpl orderFacade;

	@Test
	void orderConfirmationHydratesStoreViaBridge() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		store.setStorename("Default Store");
		Language language = new Language("en");
		Customer customer = new Customer();
		Order order = new Order();
		order.setId(1L);

		when(tenantEntityBridge.resolveStore(MerchantStoreId.of("DEFAULT"))).thenReturn(store);
		when(tenantEntityBridge.resolveLanguage(LanguageCode.of("en"))).thenReturn(language);
		when(readableCustomerMapper.convert(eq(customer), eq(store), eq(language))).thenReturn(new ReadableCustomer());

		ReadableOrderConfirmation confirmation = orderFacade.orderConfirmation(order, customer,
				MerchantStoreId.of("DEFAULT"), LanguageCode.of("en"));

		assertNotNull(confirmation);
		verify(tenantEntityBridge).resolveStore(MerchantStoreId.of("DEFAULT"));
		verify(tenantEntityBridge).resolveLanguage(LanguageCode.of("en"));
		verify(readableCustomerMapper).convert(any(Customer.class), eq(store), eq(language));
	}
}

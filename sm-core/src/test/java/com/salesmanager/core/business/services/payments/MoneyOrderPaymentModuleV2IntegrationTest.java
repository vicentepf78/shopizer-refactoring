package com.salesmanager.core.business.services.payments;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.salesmanager.core.business.modules.integration.payment.impl.MoneyOrderPayment;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.TransactionType;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.modules.integration.payment.dto.PaymentRequestContext;
import com.salesmanager.core.modules.integration.payment.dto.TransactionResult;
import com.salesmanager.core.modules.integration.payment.model.PaymentModuleV2;

class MoneyOrderPaymentModuleV2IntegrationTest {

	@Test
	void moneyOrderAuthorizeAndCaptureViaLegacyBridgeReturnsTransaction() throws Exception {
		MerchantStore store = sampleStore();
		Customer customer = sampleCustomer();
		ShoppingCartItem item = sampleCartItem();
		Payment payment = samplePayment();
		IntegrationConfiguration configuration = sampleConfiguration();
		IntegrationModule module = sampleModule();

		LegacyPaymentEntityBundle entities = LegacyPaymentEntityBundle.forPayment(store, customer,
				Collections.singletonList(item), payment, module);
		PaymentModuleV2 moduleV2 = new LegacyPaymentModuleBridge(new MoneyOrderPayment(), entities);
		PaymentRequestContext context = IntegrationContextMapper.toPaymentRequestContext(store, customer,
				Collections.singletonList(item), new BigDecimal("49.99"), payment, configuration, module);

		TransactionResult result = moduleV2.authorizeAndCapture(context);

		assertThat(result).isNotNull();
		assertThat(result.getAmount()).isEqualByComparingTo("49.99");
		assertThat(result.getTransactionType()).isEqualTo(TransactionType.AUTHORIZECAPTURE.name());
		assertThat(result.getPaymentType()).isEqualTo(PaymentType.MONEYORDER.name());
		assertThat(result.getTransactionDate()).isNotNull();
	}

	@Test
	void moneyOrderAuthorizeViaLegacyBridgeCompletesV2Path() throws Exception {
		MerchantStore store = sampleStore();
		Customer customer = sampleCustomer();
		Payment payment = samplePayment();
		IntegrationConfiguration configuration = sampleConfiguration();
		IntegrationModule module = sampleModule();

		LegacyPaymentEntityBundle entities = LegacyPaymentEntityBundle.forPayment(store, customer,
				Collections.emptyList(), payment, module);
		PaymentModuleV2 moduleV2 = new LegacyPaymentModuleBridge(new MoneyOrderPayment(), entities);
		PaymentRequestContext context = IntegrationContextMapper.toPaymentRequestContext(store, customer,
				Collections.emptyList(), new BigDecimal("10.00"), payment, configuration, module);

		TransactionResult result = moduleV2.authorize(context);

		assertThat(result).isNull();
	}

	private static MerchantStore sampleStore() {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Currency currency = new Currency();
		currency.setCurrency(java.util.Currency.getInstance("USD"));
		store.setCurrency(currency);
		Language language = new Language();
		language.setCode("en");
		store.setDefaultLanguage(language);
		return store;
	}

	private static Customer sampleCustomer() {
		Customer customer = new Customer();
		customer.setId(1L);
		customer.setEmailAddress("buyer@example.com");
		return customer;
	}

	private static ShoppingCartItem sampleCartItem() {
		ShoppingCartItem item = new ShoppingCartItem();
		item.setId(99L);
		item.setSku("MO-SKU");
		item.setQuantity(1);
		item.setItemPrice(new BigDecimal("49.99"));
		item.setProductId(500L);
		return item;
	}

	private static Payment samplePayment() {
		Payment payment = new Payment();
		payment.setModuleName("moneyorder");
		payment.setPaymentType(PaymentType.MONEYORDER);
		payment.setTransactionType(TransactionType.AUTHORIZECAPTURE);
		return payment;
	}

	private static IntegrationConfiguration sampleConfiguration() {
		IntegrationConfiguration configuration = new IntegrationConfiguration();
		configuration.setModuleCode("moneyorder");
		configuration.setActive(true);
		return configuration;
	}

	private static IntegrationModule sampleModule() {
		IntegrationModule module = new IntegrationModule();
		module.setCode("moneyorder");
		module.setModule("moneyorder");
		return module;
	}

}

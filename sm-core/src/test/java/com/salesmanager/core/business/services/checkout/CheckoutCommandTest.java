package com.salesmanager.core.business.services.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.salesmanager.contracts.customer.CustomerSnapshot;
import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotalSummary;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;

class CheckoutCommandTest {

	@Test
	void buildRequiresTenantIdentifiersCustomerSnapshotAndCartItems() {
		Customer customer = new Customer();
		CustomerSnapshot snapshot = CustomerSnapshotBuilder.from(customer);
		ShoppingCartItem item = new ShoppingCartItem();
		OrderTotalSummary summary = new OrderTotalSummary();
		summary.setTotal(BigDecimal.TEN);

		CheckoutCommand command = CheckoutCommand.builder()
				.storeId(MerchantStoreId.of("DEFAULT"))
				.languageCode(LanguageCode.of("en"))
				.customerSnapshot(snapshot)
				.customer(customer)
				.shoppingCartItems(Collections.singletonList(item))
				.orderTotalSummary(summary)
				.paymentModule("moneyorder")
				.paymentMethodType(PaymentType.MONEYORDER.name())
				.build();

		assertThat(command.getStoreId().getCode()).isEqualTo("DEFAULT");
		assertThat(command.getLanguageCode().getCode()).isEqualTo("en");
		assertThat(command.getCustomerSnapshot()).isSameAs(snapshot);
		assertThat(command.getShoppingCartItems()).hasSize(1);
	}

	@Test
	void buildRejectsBlankStorefrontPaymentFields() {
		Customer customer = new Customer();
		OrderTotalSummary summary = new OrderTotalSummary();
		summary.setTotal(BigDecimal.ONE);
		List<ShoppingCartItem> items = Collections.singletonList(new ShoppingCartItem());

		assertThrows(IllegalArgumentException.class, () -> CheckoutCommand.builder()
				.storeId(MerchantStoreId.of("DEFAULT"))
				.languageCode(LanguageCode.of("en"))
				.customerSnapshot(CustomerSnapshotBuilder.from(customer))
				.customer(customer)
				.shoppingCartItems(items)
				.orderTotalSummary(summary)
				.paymentMethodType(PaymentType.MONEYORDER.name())
				.build());

		assertThrows(IllegalArgumentException.class, () -> CheckoutCommand.builder()
				.storeId(MerchantStoreId.of("DEFAULT"))
				.languageCode(LanguageCode.of("en"))
				.customerSnapshot(CustomerSnapshotBuilder.from(customer))
				.customer(customer)
				.shoppingCartItems(items)
				.orderTotalSummary(summary)
				.paymentModule("moneyorder")
				.build());
	}

	@Test
	void buildRequiresPaymentForApiFlow() {
		Customer customer = new Customer();
		OrderTotalSummary summary = new OrderTotalSummary();
		summary.setTotal(BigDecimal.ONE);
		List<ShoppingCartItem> items = Collections.singletonList(new ShoppingCartItem());
		Order preBuilt = new Order();

		assertThrows(IllegalArgumentException.class, () -> CheckoutCommand.builder()
				.storeId(MerchantStoreId.of("DEFAULT"))
				.languageCode(LanguageCode.of("en"))
				.customerSnapshot(CustomerSnapshotBuilder.from(customer))
				.customer(customer)
				.shoppingCartItems(items)
				.orderTotalSummary(summary)
				.preBuiltOrder(preBuilt)
				.build());
	}

	@Test
	void buildAcceptsApiFlowWithPreBuiltOrderAndPayment() {
		Customer customer = new Customer();
		OrderTotalSummary summary = new OrderTotalSummary();
		summary.setTotal(BigDecimal.ONE);
		Payment payment = new Payment();

		CheckoutCommand command = CheckoutCommand.builder()
				.storeId(MerchantStoreId.of("DEFAULT"))
				.languageCode(LanguageCode.of("en"))
				.customerSnapshot(CustomerSnapshotBuilder.from(customer))
				.customer(customer)
				.shoppingCartItems(Collections.singletonList(new ShoppingCartItem()))
				.orderTotalSummary(summary)
				.preBuiltOrder(new Order())
				.payment(payment)
				.build();

		assertThat(command.isApiFlow()).isTrue();
		assertThat(command.getPayment()).isSameAs(payment);
	}

}

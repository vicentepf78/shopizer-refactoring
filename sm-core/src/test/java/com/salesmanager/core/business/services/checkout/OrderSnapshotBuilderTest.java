package com.salesmanager.core.business.services.checkout;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.salesmanager.contracts.order.OrderLineSnapshot;
import com.salesmanager.contracts.order.OrderSnapshot;
import com.salesmanager.contracts.order.OrderTotalSnapshot;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotal;
import com.salesmanager.core.model.order.OrderTotalType;
import com.salesmanager.core.model.order.orderproduct.OrderProduct;
import com.salesmanager.core.model.order.orderstatus.OrderStatus;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.reference.currency.Currency;

class OrderSnapshotBuilderTest {

	@Test
	void fromMapsStatusTotalsAndLineSkuQuantity() {
		Order order = sampleOrder();

		OrderSnapshot snapshot = OrderSnapshotBuilder.from(order);

		assertThat(snapshot.getStatus()).isEqualTo("ORDERED");
		assertThat(snapshot.getTotal()).isEqualByComparingTo("119.98");
		assertThat(snapshot.getStoreCode()).isEqualTo("DEFAULT");
		assertThat(snapshot.getCurrencyCode()).isEqualTo("USD");
		assertThat(snapshot.getPaymentType()).isEqualTo("CREDITCARD");
		assertThat(snapshot.getLines()).hasSize(1);
		OrderLineSnapshot line = snapshot.getLines().get(0);
		assertThat(line.getSku()).isEqualTo("SKU-1");
		assertThat(line.getQuantity()).isEqualTo(2);
		assertThat(line.getOneTimeCharge()).isEqualByComparingTo("59.99");
		assertThat(snapshot.getTotals()).extracting(OrderTotalSnapshot::getOrderTotalType).contains("TOTAL");
	}

	@Test
	void fromNullOrderReturnsNull() {
		assertThat(OrderSnapshotBuilder.from(null)).isNull();
	}

	private static Order sampleOrder() {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");

		Currency currency = new Currency();
		currency.setCurrency(java.util.Currency.getInstance("USD"));

		OrderProduct product = new OrderProduct();
		product.setSku("SKU-1");
		product.setProductName("Sample");
		product.setProductQuantity(2);
		product.setOneTimeCharge(new BigDecimal("59.99"));

		OrderTotal total = new OrderTotal();
		total.setOrderTotalCode("order.total.total");
		total.setTitle("Total");
		total.setValue(new BigDecimal("119.98"));
		total.setOrderTotalType(OrderTotalType.TOTAL);
		total.setSortOrder(10);

		Set<OrderProduct> products = new LinkedHashSet<>();
		products.add(product);

		Set<OrderTotal> totals = new LinkedHashSet<>();
		totals.add(total);

		Order order = new Order();
		order.setId(501L);
		order.setStatus(OrderStatus.ORDERED);
		order.setCustomerId(9L);
		order.setCustomerEmailAddress("buyer@example.com");
		order.setTotal(new BigDecimal("119.98"));
		order.setMerchant(store);
		order.setCurrency(currency);
		order.setPaymentType(PaymentType.CREDITCARD);
		order.setPaymentModuleCode("paypal");
		order.setOrderProducts(products);
		order.setOrderTotal(totals);

		return order;
	}

}

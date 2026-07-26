package com.salesmanager.core.business.services.checkout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.salesmanager.contracts.order.OrderLineSnapshot;
import com.salesmanager.contracts.order.OrderSnapshot;
import com.salesmanager.contracts.order.OrderTotalSnapshot;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotal;
import com.salesmanager.core.model.order.orderproduct.OrderProduct;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.reference.currency.Currency;

public final class OrderSnapshotBuilder {

	private OrderSnapshotBuilder() {
	}

	public static OrderSnapshot from(Order order) {
		if (order == null) {
			return null;
		}

		OrderSnapshot snapshot = new OrderSnapshot();
		snapshot.setId(order.getId());
		if (order.getStatus() != null) {
			snapshot.setStatus(order.getStatus().name());
		}
		snapshot.setCustomerId(order.getCustomerId());
		snapshot.setCustomerEmailAddress(order.getCustomerEmailAddress());
		snapshot.setCurrencyValue(order.getCurrencyValue());
		snapshot.setTotal(order.getTotal());
		snapshot.setShoppingCartCode(order.getShoppingCartCode());

		MerchantStore merchant = order.getMerchant();
		if (merchant != null) {
			snapshot.setStoreCode(merchant.getCode());
		}

		Currency currency = order.getCurrency();
		if (currency != null) {
			snapshot.setCurrencyCode(currency.getCode());
		}

		PaymentType paymentType = order.getPaymentType();
		if (paymentType != null) {
			snapshot.setPaymentType(paymentType.name());
		}
		snapshot.setPaymentModuleCode(order.getPaymentModuleCode());
		snapshot.setShippingModuleCode(order.getShippingModuleCode());
		snapshot.setLines(mapLines(order.getOrderProducts()));
		snapshot.setTotals(mapTotals(order.getOrderTotal()));

		return snapshot;
	}

	private static List<OrderLineSnapshot> mapLines(Iterable<OrderProduct> products) {
		List<OrderLineSnapshot> lines = new ArrayList<>();
		if (products == null) {
			return lines;
		}
		for (OrderProduct product : products) {
			OrderLineSnapshot line = new OrderLineSnapshot();
			line.setSku(product.getSku());
			line.setProductName(product.getProductName());
			line.setQuantity(product.getProductQuantity());
			line.setOneTimeCharge(product.getOneTimeCharge());
			lines.add(line);
		}
		return lines;
	}

	private static List<OrderTotalSnapshot> mapTotals(Iterable<OrderTotal> totals) {
		List<OrderTotalSnapshot> mapped = new ArrayList<>();
		if (totals == null) {
			return mapped;
		}
		for (OrderTotal total : totals) {
			OrderTotalSnapshot snapshot = new OrderTotalSnapshot();
			snapshot.setCode(total.getOrderTotalCode());
			snapshot.setTitle(total.getTitle());
			snapshot.setText(total.getText());
			snapshot.setValue(total.getValue());
			snapshot.setModule(total.getModule());
			if (total.getOrderTotalType() != null) {
				snapshot.setOrderTotalType(total.getOrderTotalType().name());
			}
			snapshot.setSortOrder(total.getSortOrder());
			mapped.add(snapshot);
		}
		mapped.sort(Comparator.comparingInt(OrderTotalSnapshot::getSortOrder));
		return mapped;
	}

}

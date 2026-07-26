package com.salesmanager.core.business.services.checkout;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.salesmanager.core.business.services.checkout.outbox.CheckoutOutboxEventType;
import com.salesmanager.core.business.services.checkout.outbox.CheckoutOutboxPayloadBuilder;
import com.salesmanager.core.business.services.checkout.outbox.CheckoutOutboxProperties;
import com.salesmanager.core.business.services.checkout.outbox.CheckoutOutboxRepository;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.order.OrderService;
import com.salesmanager.core.business.services.payments.PaymentService;
import com.salesmanager.core.business.services.payments.TransactionService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;
import com.salesmanager.core.model.common.UserContext;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotalSummary;
import com.salesmanager.core.model.order.orderproduct.OrderProduct;
import com.salesmanager.core.model.order.orderstatus.OrderStatus;
import com.salesmanager.core.model.order.orderstatus.OrderStatusHistory;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;

@Service
public class CheckoutStagedOrderProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutStagedOrderProcessor.class);

	private final OrderService orderService;
	private final PaymentService paymentService;
	private final CustomerService customerService;
	private final TransactionService transactionService;
	private final ProductService productService;
	private final CheckoutOutboxRepository outboxRepository;
	private final CheckoutOutboxProperties outboxProperties;

	@Inject
	public CheckoutStagedOrderProcessor(OrderService orderService, PaymentService paymentService,
			CustomerService customerService, TransactionService transactionService, ProductService productService,
			CheckoutOutboxRepository outboxRepository, CheckoutOutboxProperties outboxProperties) {
		this.orderService = orderService;
		this.paymentService = paymentService;
		this.customerService = customerService;
		this.transactionService = transactionService;
		this.productService = productService;
		this.outboxRepository = outboxRepository;
		this.outboxProperties = outboxProperties;
	}

	@Transactional(rollbackFor = ServiceException.class)
	public Order processOrder(Order order, Customer customer, List<ShoppingCartItem> items, OrderTotalSummary summary,
			Payment payment, Transaction transaction, MerchantStore store, String idempotencyKey) throws ServiceException {
		validate(order, customer, items, payment, store, summary);

		applyIpAddress(order);

		boolean writeOutbox = outboxProperties.isEnabled();
		String aggregateId = writeOutbox ? resolveAggregateId(order, idempotencyKey) : null;

		if (writeOutbox) {
			appendOutbox(aggregateId, CheckoutOutboxEventType.PAYMENT_REQUESTED,
					paymentRequestedPayload(customer, payment, store, order));
		}

		Transaction processTransaction = paymentService.processPayment(customer, store, payment, items, order);

		if (writeOutbox) {
			appendOutbox(aggregateId, CheckoutOutboxEventType.PAYMENT_CONFIRMED,
					paymentConfirmedPayload(customer, payment, processTransaction));
		}

		ensureOrderHistory(order);
		persistCustomerIfNeeded(customer);
		order.setCustomerId(customer.getId());
		orderService.create(order);

		if (transaction != null) {
			transaction.setOrder(order);
			saveOrUpdateTransaction(transaction);
		}

		if (processTransaction != null) {
			processTransaction.setOrder(order);
			saveOrUpdateTransaction(processTransaction);
		}

		if (writeOutbox) {
			appendOutbox(aggregateId, CheckoutOutboxEventType.ORDER_PERSISTED, orderPersistedPayload(order));
		}

		decrementInventory(order);

		if (writeOutbox) {
			appendOutbox(aggregateId, CheckoutOutboxEventType.INVENTORY_DECREMENTED, inventoryDecrementedPayload(order));
		}

		return order;
	}

	private static void validate(Order order, Customer customer, List<ShoppingCartItem> items, Payment payment,
			MerchantStore store, OrderTotalSummary summary) {
		Validate.notNull(order, "Order cannot be null");
		Validate.notNull(customer, "Customer cannot be null (even if anonymous order)");
		Validate.notEmpty(items, "ShoppingCart items cannot be null");
		Validate.notNull(payment, "Payment cannot be null");
		Validate.notNull(store, "MerchantStore cannot be null");
		Validate.notNull(summary, "Order total Summary cannot be null");
	}

	private static void applyIpAddress(Order order) {
		UserContext context = UserContext.getCurrentInstance();
		if (context != null) {
			String ipAddress = context.getIpAddress();
			if (!StringUtils.isBlank(ipAddress)) {
				order.setIpAddress(ipAddress);
			}
		}
	}

	private static String resolveAggregateId(Order order, String idempotencyKey) throws ServiceException {
		if (order.getId() != null && order.getId() > 0) {
			return String.valueOf(order.getId());
		}
		if (!StringUtils.isBlank(order.getShoppingCartCode())) {
			return order.getShoppingCartCode();
		}
		if (!StringUtils.isBlank(idempotencyKey)) {
			return idempotencyKey;
		}
		throw new ServiceException("checkout.outbox.aggregate-id-required");
	}

	private void appendOutbox(String aggregateId, CheckoutOutboxEventType type, String payloadJson)
			throws ServiceException {
		try {
			outboxRepository.append(aggregateId, type, payloadJson);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	private String paymentRequestedPayload(Customer customer, Payment payment, MerchantStore store, Order order)
			throws ServiceException {
		try {
			return CheckoutOutboxPayloadBuilder.paymentRequested(customer, payment, store, order.getShoppingCartCode());
		} catch (JsonProcessingException e) {
			throw new ServiceException(e);
		}
	}

	private String paymentConfirmedPayload(Customer customer, Payment payment, Transaction processTransaction)
			throws ServiceException {
		try {
			return CheckoutOutboxPayloadBuilder.paymentConfirmed(customer, payment, processTransaction);
		} catch (JsonProcessingException e) {
			throw new ServiceException(e);
		}
	}

	private String orderPersistedPayload(Order order) throws ServiceException {
		try {
			return CheckoutOutboxPayloadBuilder.orderPersisted(order);
		} catch (JsonProcessingException e) {
			throw new ServiceException(e);
		}
	}

	private String inventoryDecrementedPayload(Order order) throws ServiceException {
		try {
			return CheckoutOutboxPayloadBuilder.inventoryDecremented(order);
		} catch (JsonProcessingException e) {
			throw new ServiceException(e);
		}
	}

	private static void ensureOrderHistory(Order order) {
		if (order.getOrderHistory() == null || order.getOrderHistory().size() == 0 || order.getStatus() == null) {
			OrderStatus status = order.getStatus();
			if (status == null) {
				status = OrderStatus.ORDERED;
				order.setStatus(status);
			}
			Set<OrderStatusHistory> statusHistorySet = new HashSet<OrderStatusHistory>();
			OrderStatusHistory statusHistory = new OrderStatusHistory();
			statusHistory.setStatus(status);
			statusHistory.setDateAdded(new Date());
			statusHistory.setOrder(order);
			statusHistorySet.add(statusHistory);
			order.setOrderHistory(statusHistorySet);
		}
	}

	private void persistCustomerIfNeeded(Customer customer) throws ServiceException {
		if (customer.getId() == null || customer.getId() == 0) {
			customerService.create(customer);
		}
	}

	private void saveOrUpdateTransaction(Transaction transaction) throws ServiceException {
		if (transaction.getId() == null || transaction.getId() == 0) {
			transactionService.create(transaction);
		} else {
			transactionService.update(transaction);
		}
	}

	private void decrementInventory(Order order) throws ServiceException {
		LOGGER.debug("Update inventory");
		Set<OrderProduct> products = order.getOrderProducts();
		for (OrderProduct orderProduct : products) {
			orderProduct.getProductQuantity();
			Product p = productService.getById(orderProduct.getId());
			if (p == null) {
				throw new ServiceException(ServiceException.EXCEPTION_INVENTORY_MISMATCH);
			}
			for (ProductAvailability availability : p.getAvailabilities()) {
				int qty = availability.getProductQuantity();
				if (qty < orderProduct.getProductQuantity()) {
					LOGGER.error("APP-BACKEND [" + ServiceException.EXCEPTION_INVENTORY_MISMATCH + "]");
				}
				qty = qty - orderProduct.getProductQuantity();
				availability.setProductQuantity(qty);
			}
			productService.update(p);
		}
	}

}

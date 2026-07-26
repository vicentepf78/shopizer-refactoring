package com.salesmanager.core.business.services.checkout.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.checkout.CheckoutStagedOrderProcessor;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.order.OrderService;
import com.salesmanager.core.business.services.payments.PaymentService;
import com.salesmanager.core.business.services.payments.TransactionService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotalSummary;
import com.salesmanager.core.model.order.orderproduct.OrderProduct;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;

@ExtendWith(MockitoExtension.class)
class CheckoutStagedOrderProcessorTest {

	@Mock
	private OrderService orderService;
	@Mock
	private PaymentService paymentService;
	@Mock
	private CustomerService customerService;
	@Mock
	private TransactionService transactionService;
	@Mock
	private ProductService productService;
	@Mock
	private CheckoutOutboxRepository outboxRepository;
	@Mock
	private CheckoutOutboxProperties outboxProperties;

	private CheckoutStagedOrderProcessor processor;

	@BeforeEach
	void setUp() {
		processor = new CheckoutStagedOrderProcessor(orderService, paymentService, customerService, transactionService,
				productService, outboxRepository, outboxProperties);
	}

	@Test
	void writesFourOutboxEventsWhenFlagEnabled() throws Exception {
		when(outboxProperties.isEnabled()).thenReturn(true);

		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Customer customer = new Customer();
		customer.setId(1L);
		Order order = new Order();
		order.setShoppingCartCode("cart-99");
		OrderProduct line = new OrderProduct();
		line.setId(10L);
		line.setProductQuantity(1);
		order.setOrderProducts(new LinkedHashSet<>(Collections.singleton(line)));

		Payment payment = new Payment();
		payment.setPaymentType(PaymentType.MONEYORDER);
		payment.setModuleName("moneyorder");
		ShoppingCartItem item = new ShoppingCartItem();
		OrderTotalSummary summary = new OrderTotalSummary();
		summary.setTotal(BigDecimal.TEN);
		Transaction tx = new Transaction();

		when(paymentService.processPayment(eq(customer), eq(store), eq(payment), any(), eq(order))).thenReturn(tx);

		Product product = new Product();
		ProductAvailability availability = new ProductAvailability();
		availability.setProductQuantity(5);
		product.setAvailabilities(Collections.singleton(availability));
		when(productService.getById(10L)).thenReturn(product);

		processor.processOrder(order, customer, Collections.singletonList(item), summary, payment, null, store, null);

		verify(outboxRepository).append(eq("cart-99"), eq(CheckoutOutboxEventType.PAYMENT_REQUESTED), any());
		verify(outboxRepository).append(eq("cart-99"), eq(CheckoutOutboxEventType.PAYMENT_CONFIRMED), any());
		verify(outboxRepository).append(eq("cart-99"), eq(CheckoutOutboxEventType.ORDER_PERSISTED), any());
		verify(outboxRepository).append(eq("cart-99"), eq(CheckoutOutboxEventType.INVENTORY_DECREMENTED), any());
		verify(outboxRepository, times(4)).append(any(), any(), any());
	}

	@Test
	void outboxCustomerSnapshotUsesLanguageFieldName() throws Exception {
		when(outboxProperties.isEnabled()).thenReturn(true);

		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Customer customer = new Customer();
		customer.setId(1L);
		customer.setDefaultLanguage(new Language("en"));
		Order order = new Order();
		order.setShoppingCartCode("cart-99");
		OrderProduct line = new OrderProduct();
		line.setId(10L);
		line.setProductQuantity(1);
		order.setOrderProducts(new LinkedHashSet<>(Collections.singleton(line)));

		Payment payment = new Payment();
		payment.setPaymentType(PaymentType.MONEYORDER);
		payment.setModuleName("moneyorder");
		ShoppingCartItem item = new ShoppingCartItem();
		OrderTotalSummary summary = new OrderTotalSummary();
		summary.setTotal(BigDecimal.TEN);
		Transaction tx = new Transaction();

		when(paymentService.processPayment(eq(customer), eq(store), eq(payment), any(), eq(order))).thenReturn(tx);

		Product product = new Product();
		ProductAvailability availability = new ProductAvailability();
		availability.setProductQuantity(5);
		product.setAvailabilities(Collections.singleton(availability));
		when(productService.getById(10L)).thenReturn(product);

		processor.processOrder(order, customer, Collections.singletonList(item), summary, payment, null, store, null);

		ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
		verify(outboxRepository).append(eq("cart-99"), eq(CheckoutOutboxEventType.PAYMENT_REQUESTED), payloadCaptor.capture());

		JsonNode tree = new ObjectMapper().readTree(payloadCaptor.getValue());
		assertEquals("en", tree.get("customer").get("language").asText());
		assertFalse(tree.get("customer").has("languageCode"));
	}

	@Test
	void usesIdempotencyKeyWhenOrderHasNoIdOrCartCode() throws Exception {
		when(outboxProperties.isEnabled()).thenReturn(true);

		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Customer customer = new Customer();
		customer.setId(1L);
		Order order = new Order();
		OrderProduct line = new OrderProduct();
		line.setId(10L);
		line.setProductQuantity(1);
		order.setOrderProducts(new LinkedHashSet<>(Collections.singleton(line)));

		Payment payment = new Payment();
		payment.setPaymentType(PaymentType.MONEYORDER);
		payment.setModuleName("moneyorder");
		ShoppingCartItem item = new ShoppingCartItem();
		OrderTotalSummary summary = new OrderTotalSummary();
		summary.setTotal(BigDecimal.TEN);
		Transaction tx = new Transaction();

		when(paymentService.processPayment(eq(customer), eq(store), eq(payment), any(), eq(order))).thenReturn(tx);

		Product product = new Product();
		ProductAvailability availability = new ProductAvailability();
		availability.setProductQuantity(5);
		product.setAvailabilities(Collections.singleton(availability));
		when(productService.getById(10L)).thenReturn(product);

		processor.processOrder(order, customer, Collections.singletonList(item), summary, payment, null, store,
				"corr-retry-1");

		verify(outboxRepository).append(eq("corr-retry-1"), eq(CheckoutOutboxEventType.PAYMENT_REQUESTED), any());
		verify(outboxRepository, times(4)).append(eq("corr-retry-1"), any(), any());
	}

	@Test
	void throwsWhenOutboxEnabledAndNoStableAggregateId() {
		when(outboxProperties.isEnabled()).thenReturn(true);

		MerchantStore store = new MerchantStore();
		Customer customer = new Customer();
		Order order = new Order();
		Payment payment = new Payment();
		ShoppingCartItem item = new ShoppingCartItem();
		OrderTotalSummary summary = new OrderTotalSummary();
		summary.setTotal(BigDecimal.TEN);

		ServiceException error = assertThrows(ServiceException.class,
				() -> processor.processOrder(order, customer, Collections.singletonList(item), summary, payment, null,
						store, null));

		assertEquals("checkout.outbox.aggregate-id-required", error.getMessageCode());
	}

}

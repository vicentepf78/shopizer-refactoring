package com.salesmanager.core.business.services.checkout.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.checkout.CheckoutOutboxJpaRepository;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.checkout.CheckoutStagedOrderProcessor;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.order.OrderService;
import com.salesmanager.core.business.services.payments.PaymentService;
import com.salesmanager.core.business.services.payments.TransactionService;
import com.salesmanager.core.model.checkout.outbox.CheckoutOutboxEvent;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotalSummary;
import com.salesmanager.core.model.order.orderproduct.OrderProduct;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;

@DataJpaTest
@Import({ CheckoutOutboxRepositoryImpl.class, CheckoutStagedOrderProcessor.class, CheckoutOutboxProperties.class })
@ContextConfiguration(classes = CheckoutStagedOrderProcessorRollbackIntegrationTest.Config.class)
@TestPropertySource(properties = {
		"checkout.outbox.enabled=true",
		"spring.jpa.properties.hibernate.default_schema=",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class CheckoutStagedOrderProcessorRollbackIntegrationTest {

	@EnableJpaRepositories(basePackageClasses = CheckoutOutboxJpaRepository.class)
	@EntityScan(basePackageClasses = CheckoutOutboxEvent.class)
	static class Config {
	}

	@Autowired
	private CheckoutStagedOrderProcessor processor;

	@Autowired
	private CheckoutOutboxJpaRepository jpaRepository;

	@MockBean
	private OrderService orderService;
	@MockBean
	private PaymentService paymentService;
	@MockBean
	private CustomerService customerService;
	@MockBean
	private TransactionService transactionService;
	@MockBean
	private ProductService productService;

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void rollsBackOutboxRowsWhenPaymentDeclines() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Customer customer = new Customer();
		customer.setId(1L);
		Order order = new Order();
		order.setShoppingCartCode("cart-rollback");
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

		when(paymentService.processPayment(eq(customer), eq(store), eq(payment), any(), eq(order)))
				.thenThrow(new ServiceException(ServiceException.EXCEPTION_PAYMENT_DECLINED));

		assertThrows(ServiceException.class,
				() -> processor.processOrder(order, customer, Collections.singletonList(item), summary, payment, null,
						store, null));

		assertThat(jpaRepository.findAll()).isEmpty();
	}

}

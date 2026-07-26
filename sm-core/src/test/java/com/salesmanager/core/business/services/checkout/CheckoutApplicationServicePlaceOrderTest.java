package com.salesmanager.core.business.services.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.attribute.ProductAttributeService;
import com.salesmanager.core.business.services.catalog.product.file.DigitalProductService;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.order.OrderService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;
import com.salesmanager.core.model.catalog.product.description.ProductDescription;
import com.salesmanager.core.model.catalog.product.price.FinalPrice;
import com.salesmanager.core.model.catalog.product.price.ProductPrice;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotalSummary;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;

@ExtendWith(MockitoExtension.class)
class CheckoutApplicationServicePlaceOrderTest {

	@Mock
	private OrderService orderService;
	@Mock
	private ProductService productService;
	@Mock
	private ProductAttributeService productAttributeService;
	@Mock
	private DigitalProductService digitalProductService;
	@Mock
	private MerchantStoreService merchantStoreService;
	@Mock
	private LanguageService languageService;

	private CheckoutApplicationService checkoutApplicationService;

	@BeforeEach
	void setUp() {
		checkoutApplicationService = new CheckoutApplicationServiceImpl(orderService, productService,
				productAttributeService, digitalProductService, merchantStoreService, languageService);
	}

	@Test
	void apiFlowDelegatesToOrderServiceWithPreBuiltOrder() throws Exception {
		MerchantStore store = store("DEFAULT");
		Language language = new Language("en");
		Customer customer = new Customer();
		Order preBuilt = new Order();
		Order persisted = new Order();
		persisted.setId(99L);
		OrderTotalSummary summary = summary(BigDecimal.TEN);
		Payment payment = new Payment();
		ShoppingCartItem item = new ShoppingCartItem();

		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		when(languageService.getByCode("en")).thenReturn(language);
		when(orderService.processOrder(eq(preBuilt), eq(customer), any(), eq(summary), eq(payment), eq(store)))
				.thenReturn(persisted);

		CheckoutCommand command = CheckoutCommand.builder()
				.storeId(MerchantStoreId.of("DEFAULT"))
				.languageCode(LanguageCode.of("en"))
				.customerSnapshot(CustomerSnapshotBuilder.from(customer))
				.customer(customer)
				.shoppingCartItems(Collections.singletonList(item))
				.orderTotalSummary(summary)
				.preBuiltOrder(preBuilt)
				.payment(payment)
				.build();

		Order result = checkoutApplicationService.placeOrder(command);

		assertThat(result.getId()).isEqualTo(99L);
		verify(orderService).processOrder(preBuilt, customer, command.getShoppingCartItems(), summary, payment, store);
	}

	@Test
	void storefrontPayPalWithoutTransactionThrowsPaymentError() throws Exception {
		MerchantStore store = store("DEFAULT");
		Language language = new Language("en");
		Customer customer = new Customer();
		OrderTotalSummary summary = summary(BigDecimal.TEN);
		ShoppingCartItem item = cartItem("SKU-1");

		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		when(languageService.getByCode("en")).thenReturn(language);
		when(productService.getBySku("SKU-1", store, language)).thenReturn(productWithInventory(store, 10, item.getQuantity()));
		when(digitalProductService.getByProduct(eq(store), any(Product.class))).thenReturn(null);

		CheckoutCommand command = CheckoutCommand.builder()
				.storeId(MerchantStoreId.of("DEFAULT"))
				.languageCode(LanguageCode.of("en"))
				.customerSnapshot(CustomerSnapshotBuilder.from(customer))
				.customer(customer)
				.shoppingCartItems(Collections.singletonList(item))
				.orderTotalSummary(summary)
				.paymentModule("paypal")
				.paymentMethodType(PaymentType.PAYPAL.name())
				.build();

		ServiceException error = assertThrows(ServiceException.class, () -> checkoutApplicationService.placeOrder(command));

		assertThat(error.getMessageCode()).isEqualTo("payment.error");
	}

	@Test
	void storefrontMissingProductThrowsInventoryMismatch() throws Exception {
		MerchantStore store = store("DEFAULT");
		Language language = new Language("en");
		Customer customer = new Customer();
		OrderTotalSummary summary = summary(BigDecimal.TEN);
		ShoppingCartItem item = cartItem("MISSING");

		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		when(languageService.getByCode("en")).thenReturn(language);
		when(productService.getBySku("MISSING", store, language)).thenReturn(null);

		CheckoutCommand command = CheckoutCommand.builder()
				.storeId(MerchantStoreId.of("DEFAULT"))
				.languageCode(LanguageCode.of("en"))
				.customerSnapshot(CustomerSnapshotBuilder.from(customer))
				.customer(customer)
				.shoppingCartItems(Collections.singletonList(item))
				.orderTotalSummary(summary)
				.paymentModule("moneyorder")
				.paymentMethodType(PaymentType.MONEYORDER.name())
				.build();

		ServiceException error = assertThrows(ServiceException.class, () -> checkoutApplicationService.placeOrder(command));

		assertThat(error.getExceptionType()).isEqualTo(ServiceException.EXCEPTION_INVENTORY_MISMATCH);
	}

	@Test
	void storefrontWithTransactionUsesLegacyOrderServiceBranch() throws Exception {
		MerchantStore store = store("DEFAULT");
		Language language = new Language("en");
		Customer customer = new Customer();
		OrderTotalSummary summary = summary(BigDecimal.TEN);
		ShoppingCartItem item = cartItem("SKU-1");
		Transaction transaction = new Transaction();
		transaction.setTransactionDetails(details("PAYERID", "TOKEN"));

		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		when(languageService.getByCode("en")).thenReturn(language);
		when(productService.getBySku("SKU-1", store, language)).thenReturn(productWithInventory(store, 10, item.getQuantity()));
		when(digitalProductService.getByProduct(eq(store), any(Product.class))).thenReturn(null);
		when(orderService.processOrder(any(Order.class), eq(customer), any(), eq(summary), any(Payment.class), eq(store)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		CheckoutCommand command = CheckoutCommand.builder()
				.storeId(MerchantStoreId.of("DEFAULT"))
				.languageCode(LanguageCode.of("en"))
				.customerSnapshot(CustomerSnapshotBuilder.from(customer))
				.customer(customer)
				.shoppingCartItems(Collections.singletonList(item))
				.orderTotalSummary(summary)
				.transaction(transaction)
				.paymentModule("paypal")
				.paymentMethodType(PaymentType.PAYPAL.name())
				.build();

		checkoutApplicationService.placeOrder(command);

		verify(orderService).processOrder(any(Order.class), eq(customer), any(), eq(summary), any(Payment.class), eq(store));
	}

	@Test
	void storefrontWithoutTransactionUsesTransactionOverloadWithNull() throws Exception {
		MerchantStore store = store("DEFAULT");
		Language language = new Language("en");
		Customer customer = new Customer();
		OrderTotalSummary summary = summary(BigDecimal.TEN);
		ShoppingCartItem item = cartItem("SKU-1");

		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		when(languageService.getByCode("en")).thenReturn(language);
		when(productService.getBySku("SKU-1", store, language)).thenReturn(productWithInventory(store, 10, item.getQuantity()));
		when(digitalProductService.getByProduct(eq(store), any(Product.class))).thenReturn(null);
		when(orderService.processOrder(any(Order.class), eq(customer), any(), eq(summary), any(Payment.class), isNull(),
				eq(store))).thenAnswer(invocation -> invocation.getArgument(0));

		CheckoutCommand command = CheckoutCommand.builder()
				.storeId(MerchantStoreId.of("DEFAULT"))
				.languageCode(LanguageCode.of("en"))
				.customerSnapshot(CustomerSnapshotBuilder.from(customer))
				.customer(customer)
				.shoppingCartItems(Collections.singletonList(item))
				.orderTotalSummary(summary)
				.paymentModule("moneyorder")
				.paymentMethodType(PaymentType.MONEYORDER.name())
				.build();

		checkoutApplicationService.placeOrder(command);

		verify(orderService).processOrder(any(Order.class), eq(customer), any(), eq(summary), any(Payment.class),
				isNull(), eq(store));
	}

	private static MerchantStore store(String code) {
		MerchantStore store = new MerchantStore();
		store.setId(1);
		store.setCode(code);
		store.setDefaultLanguage(new Language("en"));
		return store;
	}

	private static OrderTotalSummary summary(BigDecimal total) {
		OrderTotalSummary summary = new OrderTotalSummary();
		summary.setTotal(total);
		summary.setTotals(Collections.emptyList());
		return summary;
	}

	private static ShoppingCartItem cartItem(String sku) {
		ShoppingCartItem item = new ShoppingCartItem();
		item.setSku(sku);
		item.setQuantity(1);
		item.setItemPrice(BigDecimal.TEN);

		Product product = new Product();
		product.setSku(sku);
		ProductDescription description = new ProductDescription();
		description.setName("Test product");
		product.setDescriptions(Collections.singleton(description));
		item.setProduct(product);

		ProductPrice productPrice = new ProductPrice();
		productPrice.setDefaultPrice(true);
		productPrice.setCode("default");
		FinalPrice finalPrice = new FinalPrice();
		finalPrice.setProductPrice(productPrice);
		finalPrice.setFinalPrice(BigDecimal.TEN);
		item.setFinalPrice(finalPrice);

		return item;
	}

	private static Product productWithInventory(MerchantStore store, int qty, int requested) {
		Product product = new Product();
		product.setMerchantStore(store);
		ProductAvailability availability = new ProductAvailability();
		availability.setRegion(com.salesmanager.core.business.constants.Constants.ALL_REGIONS);
		availability.setProductQuantity(qty);
		product.setAvailabilities(Collections.singleton(availability));
		ProductDescription description = new ProductDescription();
		description.setName("Test product");
		product.setDescriptions(Collections.singleton(description));
		return product;
	}

	private static Map<String, String> details(String payerId, String token) {
		Map<String, String> details = new HashMap<String, String>();
		details.put("PAYERID", payerId);
		details.put("TOKEN", token);
		return details;
	}

}

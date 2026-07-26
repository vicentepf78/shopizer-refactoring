package com.salesmanager.core.business.services.checkout.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.attribute.ProductAttributeService;
import com.salesmanager.core.business.services.catalog.product.file.DigitalProductService;
import com.salesmanager.core.business.services.checkout.CheckoutApplicationService;
import com.salesmanager.core.business.services.checkout.CheckoutApplicationServiceImpl;
import com.salesmanager.core.business.services.checkout.CheckoutCommand;
import com.salesmanager.core.business.services.checkout.CheckoutStagedOrderProcessor;
import com.salesmanager.core.business.services.checkout.CustomerSnapshotBuilder;
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
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;

@ExtendWith(MockitoExtension.class)
class CheckoutOutboxIntegrationTest {

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
	@Mock
	private CheckoutOutboxProperties outboxProperties;
	@Mock
	private CheckoutStagedOrderProcessor stagedOrderProcessor;

	private CheckoutApplicationService checkoutApplicationService;

	@BeforeEach
	void setUp() {
		checkoutApplicationService = new CheckoutApplicationServiceImpl(orderService, productService,
				productAttributeService, digitalProductService, merchantStoreService, languageService, outboxProperties,
				stagedOrderProcessor);
	}

	@Test
	void whenOutboxEnabledUsesStagedProcessor() throws Exception {
		MerchantStore store = store("DEFAULT");
		Language language = new Language("en");
		Customer customer = new Customer();
		OrderTotalSummary summary = summary(BigDecimal.TEN);
		ShoppingCartItem item = cartItem("SKU-1");
		Order builtOrder = new Order();

		when(outboxProperties.isEnabled()).thenReturn(true);
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		when(languageService.getByCode("en")).thenReturn(language);
		when(productService.getBySku("SKU-1", store, language)).thenReturn(productWithInventory(store, 10, 1));
		when(digitalProductService.getByProduct(eq(store), any(Product.class))).thenReturn(null);
		when(stagedOrderProcessor.processOrder(any(Order.class), eq(customer), any(), eq(summary), any(Payment.class),
				isNull(), eq(store))).thenReturn(builtOrder);

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

		verify(stagedOrderProcessor).processOrder(any(Order.class), eq(customer), any(), eq(summary), any(Payment.class),
				isNull(), eq(store));
		verify(orderService, never()).processOrder(any(), any(), any(), any(), any(), any());
	}

	@Test
	void whenOutboxDisabledUsesLegacyOrderService() throws Exception {
		MerchantStore store = store("DEFAULT");
		Language language = new Language("en");
		Customer customer = new Customer();
		OrderTotalSummary summary = summary(BigDecimal.TEN);
		ShoppingCartItem item = cartItem("SKU-1");

		when(outboxProperties.isEnabled()).thenReturn(false);
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		when(languageService.getByCode("en")).thenReturn(language);
		when(productService.getBySku("SKU-1", store, language)).thenReturn(productWithInventory(store, 10, 1));
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
		verify(stagedOrderProcessor, never()).processOrder(any(), any(), any(), any(), any(), any(), any());
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

}

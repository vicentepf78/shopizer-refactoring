package com.salesmanager.core.business.services.checkout;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.attribute.ProductAttributeService;
import com.salesmanager.core.business.services.catalog.product.file.DigitalProductService;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.order.OrderService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.utils.CreditCardUtils;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotalSummary;
import com.salesmanager.core.model.order.orderproduct.OrderProduct;
import com.salesmanager.core.model.order.orderstatus.OrderStatus;
import com.salesmanager.core.model.order.orderstatus.OrderStatusHistory;
import com.salesmanager.core.model.order.payment.CreditCard;
import com.salesmanager.core.model.payments.CreditCardPayment;
import com.salesmanager.core.model.payments.CreditCardType;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.core.business.services.checkout.outbox.CheckoutOutboxProperties;

@Service("checkoutApplicationService")
public class CheckoutApplicationServiceImpl implements CheckoutApplicationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutApplicationServiceImpl.class);

	private final OrderService orderService;
	private final ProductService productService;
	private final ProductAttributeService productAttributeService;
	private final DigitalProductService digitalProductService;
	private final MerchantStoreService merchantStoreService;
	private final LanguageService languageService;
	private final CheckoutOutboxProperties outboxProperties;
	private final CheckoutStagedOrderProcessor stagedOrderProcessor;
	private final CheckoutOrderProductAssembler orderProductAssembler;

	@Inject
	public CheckoutApplicationServiceImpl(OrderService orderService, ProductService productService,
			ProductAttributeService productAttributeService, DigitalProductService digitalProductService,
			MerchantStoreService merchantStoreService, LanguageService languageService,
			CheckoutOutboxProperties outboxProperties, CheckoutStagedOrderProcessor stagedOrderProcessor) {
		this.orderService = orderService;
		this.productService = productService;
		this.productAttributeService = productAttributeService;
		this.digitalProductService = digitalProductService;
		this.merchantStoreService = merchantStoreService;
		this.languageService = languageService;
		this.outboxProperties = outboxProperties;
		this.stagedOrderProcessor = stagedOrderProcessor;
		this.orderProductAssembler = new CheckoutOrderProductAssembler(productService, digitalProductService,
				productAttributeService);
	}

	@Override
	public Order placeOrder(CheckoutCommand command) throws ServiceException {
		try {
			MerchantStore store = merchantStoreService.getByCode(command.getStoreId().getCode());
			Language language = languageService.getByCode(command.getLanguageCode().getCode());

			if (command.isApiFlow()) {
				return placeApiOrder(command, store);
			}
			return placeStorefrontOrder(command, store, language);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	private Order placeApiOrder(CheckoutCommand command, MerchantStore store) throws ServiceException {
		Order modelOrder = command.getPreBuiltOrder();
		return processCheckoutOrder(modelOrder, command.getCustomer(), command.getShoppingCartItems(),
				command.getOrderTotalSummary(), command.getPayment(), null, store);
	}

	private Order placeStorefrontOrder(CheckoutCommand command, MerchantStore store, Language language)
			throws Exception {
		Customer customer = command.getCustomer();
		List<ShoppingCartItem> shoppingCartItems = command.getShoppingCartItems();
		OrderTotalSummary summary = command.getOrderTotalSummary();
		Transaction transaction = command.getTransaction();

		Order modelOrder = new Order();
		modelOrder.setDatePurchased(new Date());
		modelOrder.setBilling(customer.getBilling());
		modelOrder.setDelivery(customer.getDelivery());
		modelOrder.setPaymentModuleCode(command.getPaymentModule());
		modelOrder.setPaymentType(PaymentType.valueOf(command.getPaymentMethodType()));
		modelOrder.setShippingModuleCode(command.getShippingModule());
		modelOrder.setCustomerAgreement(command.isCustomerAgreed());
		modelOrder.setLocale(storeLocale(store));

		Set<OrderProduct> orderProducts = new LinkedHashSet<OrderProduct>();

		if (!StringUtils.isBlank(command.getComments())) {
			OrderStatusHistory statusHistory = new OrderStatusHistory();
			statusHistory.setStatus(OrderStatus.ORDERED);
			statusHistory.setOrder(modelOrder);
			statusHistory.setDateAdded(new Date());
			statusHistory.setComments(command.getComments());
			modelOrder.getOrderHistory().add(statusHistory);
		}

		String shoppingCartCode = null;

		for (ShoppingCartItem item : shoppingCartItems) {

			if (shoppingCartCode == null && item.getShoppingCart() != null) {
				shoppingCartCode = item.getShoppingCart().getShoppingCartCode();
			}

			Product product = productService.getBySku(item.getSku(), store, language);
			if (product == null) {
				throw new ServiceException(ServiceException.EXCEPTION_INVENTORY_MISMATCH);
			}

			LOGGER.debug("Validate inventory");
			for (ProductAvailability availability : product.getAvailabilities()) {
				if (availability.getRegion().equals(Constants.ALL_REGIONS)) {
					int qty = availability.getProductQuantity();
					if (qty < item.getQuantity()) {
						throw new ServiceException(ServiceException.EXCEPTION_INVENTORY_MISMATCH);
					}
				}
			}

			OrderProduct orderProduct = new OrderProduct();
			orderProduct = orderProductAssembler.populate(item, orderProduct, store, language);
			orderProduct.setOrder(modelOrder);
			orderProducts.add(orderProduct);
		}

		modelOrder.setOrderProducts(orderProducts);

		List<com.salesmanager.core.model.order.OrderTotal> totals = summary.getTotals();

		Collections.sort(totals, new Comparator<com.salesmanager.core.model.order.OrderTotal>() {
			public int compare(com.salesmanager.core.model.order.OrderTotal x,
					com.salesmanager.core.model.order.OrderTotal y) {
				if (x.getSortOrder() == y.getSortOrder()) {
					return 0;
				}
				return x.getSortOrder() < y.getSortOrder() ? -1 : 1;
			}
		});

		Set<com.salesmanager.core.model.order.OrderTotal> modelTotals = new LinkedHashSet<com.salesmanager.core.model.order.OrderTotal>();
		for (com.salesmanager.core.model.order.OrderTotal total : totals) {
			total.setOrder(modelOrder);
			modelTotals.add(total);
		}

		modelOrder.setOrderTotal(modelTotals);
		modelOrder.setTotal(summary.getTotal());

		modelOrder.setCurrency(store.getCurrency());
		modelOrder.setMerchant(store);

		populateOrderCustomer(customer, modelOrder);

		if (!StringUtils.isBlank(command.getShippingModule())) {
			modelOrder.setShippingModuleCode(command.getShippingModule());
		}

		Payment payment = buildStorefrontPayment(command, modelOrder);

		modelOrder.setShoppingCartCode(shoppingCartCode);
		modelOrder.setPaymentModuleCode(command.getPaymentModule());
		payment.setModuleName(command.getPaymentModule());

		processCheckoutOrder(modelOrder, customer, shoppingCartItems, summary, payment, transaction, store);

		return modelOrder;
	}

	private Order processCheckoutOrder(Order modelOrder, Customer customer, List<ShoppingCartItem> shoppingCartItems,
			OrderTotalSummary summary, Payment payment, Transaction transaction, MerchantStore store)
			throws ServiceException {
		if (outboxProperties.isEnabled()) {
			// ponytail: legacy storefront PayPal drops command Transaction; outbox path must match
			return stagedOrderProcessor.processOrder(modelOrder, customer, shoppingCartItems, summary, payment, null,
					store);
		}
		if (transaction != null) {
			return orderService.processOrder(modelOrder, customer, shoppingCartItems, summary, payment, store);
		}
		return orderService.processOrder(modelOrder, customer, shoppingCartItems, summary, payment, transaction, store);
	}

	private Payment buildStorefrontPayment(CheckoutCommand command, Order modelOrder) throws Exception {
		String paymentType = command.getPaymentMethodType();
		Map<String, String> paymentDetails = command.getPaymentDetails();
		OrderTotalSummary summary = command.getOrderTotalSummary();

		Payment payment = new Payment();
		payment.setPaymentType(PaymentType.valueOf(paymentType));
		payment.setAmount(summary.getTotal());
		payment.setModuleName(command.getPaymentModule());
		payment.setCurrency(modelOrder.getCurrency());

		if (paymentDetails != null && paymentDetails.get("paymentToken") != null) {
			String paymentToken = paymentDetails.get("paymentToken");
			Map<String, String> paymentMetaData = new HashMap<String, String>();
			payment.setPaymentMetaData(paymentMetaData);
			paymentMetaData.put("paymentToken", paymentToken);
		}

		if (PaymentType.CREDITCARD.name().equals(paymentType)) {
			payment = new CreditCardPayment();
			((CreditCardPayment) payment).setCardOwner(paymentDetails.get("creditcard_card_holder"));
			((CreditCardPayment) payment).setCredidCardValidationNumber(paymentDetails.get("creditcard_card_cvv"));
			((CreditCardPayment) payment).setCreditCardNumber(paymentDetails.get("creditcard_card_number"));
			((CreditCardPayment) payment).setExpirationMonth(paymentDetails.get("creditcard_card_expirationmonth"));
			((CreditCardPayment) payment).setExpirationYear(paymentDetails.get("creditcard_card_expirationyear"));

			payment.setPaymentMetaData(paymentDetails);
			payment.setPaymentType(PaymentType.valueOf(paymentType));
			payment.setAmount(summary.getTotal());
			payment.setModuleName(command.getPaymentModule());
			payment.setCurrency(modelOrder.getCurrency());

			CreditCardType creditCardType = null;
			String cardType = paymentDetails.get("creditcard_card_type");

			if (CreditCardType.AMEX.name().equalsIgnoreCase(cardType)) {
				creditCardType = CreditCardType.AMEX;
			} else if (CreditCardType.VISA.name().equalsIgnoreCase(cardType)) {
				creditCardType = CreditCardType.VISA;
			} else if (CreditCardType.MASTERCARD.name().equalsIgnoreCase(cardType)) {
				creditCardType = CreditCardType.MASTERCARD;
			} else if (CreditCardType.DINERS.name().equalsIgnoreCase(cardType)) {
				creditCardType = CreditCardType.DINERS;
			} else if (CreditCardType.DISCOVERY.name().equalsIgnoreCase(cardType)) {
				creditCardType = CreditCardType.DISCOVERY;
			}

			((CreditCardPayment) payment).setCreditCard(creditCardType);

			if (creditCardType != null) {
				CreditCard cc = new CreditCard();
				cc.setCardType(creditCardType);
				cc.setCcCvv(((CreditCardPayment) payment).getCredidCardValidationNumber());
				cc.setCcOwner(((CreditCardPayment) payment).getCardOwner());
				cc.setCcExpires(((CreditCardPayment) payment).getExpirationMonth() + "-"
						+ ((CreditCardPayment) payment).getExpirationYear());

				if (!StringUtils.isBlank(cc.getCcNumber())) {
					String maskedNumber = CreditCardUtils.maskCardNumber(paymentDetails.get("creditcard_card_number"));
					cc.setCcNumber(maskedNumber);
					modelOrder.setCreditCard(cc);
				}
			}
		}

		if (PaymentType.PAYPAL.name().equals(paymentType)) {
			if (command.getTransaction() == null) {
				throw new ServiceException("payment.error");
			}

			payment = new com.salesmanager.core.model.payments.PaypalPayment();
			((com.salesmanager.core.model.payments.PaypalPayment) payment)
					.setPayerId(command.getTransaction().getTransactionDetails().get("PAYERID"));
			((com.salesmanager.core.model.payments.PaypalPayment) payment)
					.setPaymentToken(command.getTransaction().getTransactionDetails().get("TOKEN"));
		}

		return payment;
	}

	private static void populateOrderCustomer(Customer customer, Order order) throws Exception {
		order.setBilling(customer.getBilling());
		order.setDelivery(customer.getDelivery());
		order.setCustomerEmailAddress(customer.getEmailAddress());
		order.setCustomerId(customer.getId());
		if (!customer.isAnonymous() && !StringUtils.isBlank(customer.getPassword())) {
			customer.setNick(customer.getEmailAddress());
		}
	}

	private static Locale storeLocale(MerchantStore store) {
		Locale defaultLocale = Constants.DEFAULT_LOCALE;
		Locale[] locales = Locale.getAvailableLocales();
		for (int i = 0; i < locales.length; i++) {
			Locale l = locales[i];
			try {
				if (l.toLanguageTag().equals(store.getDefaultLanguage().getCode())) {
					defaultLocale = l;
					break;
				}
			} catch (Exception e) {
				LOGGER.error("An error occured while getting ISO code for locale " + l.toString());
			}
		}
		return defaultLocale;
	}

}

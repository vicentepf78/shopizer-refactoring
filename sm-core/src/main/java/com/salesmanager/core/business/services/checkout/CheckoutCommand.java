package com.salesmanager.core.business.services.checkout;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.salesmanager.contracts.customer.CustomerSnapshot;
import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotalSummary;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;

/**
 * Checkout place-order input. Tenant identifiers and {@link CustomerSnapshot} are contract-facing;
 * core entities remain until Wave 6 entity decoupling.
 */
public final class CheckoutCommand {

	private final MerchantStoreId storeId;
	private final LanguageCode languageCode;
	private final CustomerSnapshot customerSnapshot;
	private final Customer customer;
	private final List<ShoppingCartItem> shoppingCartItems;
	private final OrderTotalSummary orderTotalSummary;
	private final Payment payment;
	private final Transaction transaction;
	private final Order preBuiltOrder;
	private final String paymentModule;
	private final String paymentMethodType;
	private final String shippingModule;
	private final String comments;
	private final Map<String, String> paymentDetails;
	private final boolean customerAgreed;
	private final String idempotencyKey;

	private CheckoutCommand(Builder builder) {
		this.storeId = builder.storeId;
		this.languageCode = builder.languageCode;
		this.customerSnapshot = builder.customerSnapshot;
		this.customer = builder.customer;
		this.shoppingCartItems = builder.shoppingCartItems;
		this.orderTotalSummary = builder.orderTotalSummary;
		this.payment = builder.payment;
		this.transaction = builder.transaction;
		this.preBuiltOrder = builder.preBuiltOrder;
		this.paymentModule = builder.paymentModule;
		this.paymentMethodType = builder.paymentMethodType;
		this.shippingModule = builder.shippingModule;
		this.comments = builder.comments;
		this.paymentDetails = builder.paymentDetails == null ? Collections.emptyMap() : builder.paymentDetails;
		this.customerAgreed = builder.customerAgreed;
		this.idempotencyKey = builder.idempotencyKey;
	}

	public static Builder builder() {
		return new Builder();
	}

	public MerchantStoreId getStoreId() {
		return storeId;
	}

	public LanguageCode getLanguageCode() {
		return languageCode;
	}

	public CustomerSnapshot getCustomerSnapshot() {
		return customerSnapshot;
	}

	public Customer getCustomer() {
		return customer;
	}

	public List<ShoppingCartItem> getShoppingCartItems() {
		return shoppingCartItems;
	}

	public OrderTotalSummary getOrderTotalSummary() {
		return orderTotalSummary;
	}

	public Payment getPayment() {
		return payment;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public Order getPreBuiltOrder() {
		return preBuiltOrder;
	}

	public String getPaymentModule() {
		return paymentModule;
	}

	public String getPaymentMethodType() {
		return paymentMethodType;
	}

	public String getShippingModule() {
		return shippingModule;
	}

	public String getComments() {
		return comments;
	}

	public Map<String, String> getPaymentDetails() {
		return paymentDetails;
	}

	public boolean isCustomerAgreed() {
		return customerAgreed;
	}

	/**
	 * Stable key for checkout outbox aggregate id when order has no id or shoppingCartCode
	 * (typically {@code X-Correlation-Id} from the HTTP layer).
	 */
	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public boolean isApiFlow() {
		return preBuiltOrder != null;
	}

	public static final class Builder {

		private MerchantStoreId storeId;
		private LanguageCode languageCode;
		private CustomerSnapshot customerSnapshot;
		private Customer customer;
		private List<ShoppingCartItem> shoppingCartItems;
		private OrderTotalSummary orderTotalSummary;
		private Payment payment;
		private Transaction transaction;
		private Order preBuiltOrder;
		private String paymentModule;
		private String paymentMethodType;
		private String shippingModule;
		private String comments;
		private Map<String, String> paymentDetails;
		private boolean customerAgreed;
		private String idempotencyKey;

		public Builder storeId(MerchantStoreId storeId) {
			this.storeId = storeId;
			return this;
		}

		public Builder languageCode(LanguageCode languageCode) {
			this.languageCode = languageCode;
			return this;
		}

		public Builder customerSnapshot(CustomerSnapshot customerSnapshot) {
			this.customerSnapshot = customerSnapshot;
			return this;
		}

		public Builder customer(Customer customer) {
			this.customer = customer;
			return this;
		}

		public Builder shoppingCartItems(List<ShoppingCartItem> shoppingCartItems) {
			this.shoppingCartItems = shoppingCartItems;
			return this;
		}

		public Builder orderTotalSummary(OrderTotalSummary orderTotalSummary) {
			this.orderTotalSummary = orderTotalSummary;
			return this;
		}

		public Builder payment(Payment payment) {
			this.payment = payment;
			return this;
		}

		public Builder transaction(Transaction transaction) {
			this.transaction = transaction;
			return this;
		}

		public Builder preBuiltOrder(Order preBuiltOrder) {
			this.preBuiltOrder = preBuiltOrder;
			return this;
		}

		public Builder paymentModule(String paymentModule) {
			this.paymentModule = paymentModule;
			return this;
		}

		public Builder paymentMethodType(String paymentMethodType) {
			this.paymentMethodType = paymentMethodType;
			return this;
		}

		public Builder shippingModule(String shippingModule) {
			this.shippingModule = shippingModule;
			return this;
		}

		public Builder comments(String comments) {
			this.comments = comments;
			return this;
		}

		public Builder paymentDetails(Map<String, String> paymentDetails) {
			this.paymentDetails = paymentDetails;
			return this;
		}

		public Builder customerAgreed(boolean customerAgreed) {
			this.customerAgreed = customerAgreed;
			return this;
		}

		public Builder idempotencyKey(String idempotencyKey) {
			this.idempotencyKey = idempotencyKey;
			return this;
		}

		public CheckoutCommand build() {
			Validate.notNull(storeId, "storeId is required");
			Validate.notNull(languageCode, "languageCode is required");
			Validate.notNull(customerSnapshot, "customerSnapshot is required");
			Validate.notNull(customer, "customer is required");
			Validate.notEmpty(shoppingCartItems, "shoppingCartItems is required");
			Validate.notNull(orderTotalSummary, "orderTotalSummary is required");

			if (preBuiltOrder != null) {
				if (payment == null) {
					throw new IllegalArgumentException("payment is required for api checkout flow");
				}
			} else {
				if (StringUtils.isBlank(paymentMethodType)) {
					throw new IllegalArgumentException("paymentMethodType is required for storefront checkout flow");
				}
				if (StringUtils.isBlank(paymentModule)) {
					throw new IllegalArgumentException("paymentModule is required for storefront checkout flow");
				}
			}

			return new CheckoutCommand(this);
		}
	}

}

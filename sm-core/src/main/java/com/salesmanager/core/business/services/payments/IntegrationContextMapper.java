package com.salesmanager.core.business.services.payments;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.model.common.Delivery;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.payments.TransactionType;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.core.model.shipping.PackageDetails;
import com.salesmanager.core.model.shipping.ShippingOption;
import com.salesmanager.core.model.shipping.ShippingOrigin;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.modules.integration.common.dto.IntegrationStoreContext;
import com.salesmanager.core.modules.integration.payment.dto.PaymentCaptureContext;
import com.salesmanager.core.modules.integration.payment.dto.PaymentLineItemDto;
import com.salesmanager.core.modules.integration.payment.dto.PaymentRefundContext;
import com.salesmanager.core.modules.integration.payment.dto.PaymentRequestContext;
import com.salesmanager.core.modules.integration.payment.dto.TransactionResult;
import com.salesmanager.core.modules.integration.shipping.dto.PackageDetailsDto;
import com.salesmanager.core.modules.integration.shipping.dto.ShippingAddressDto;
import com.salesmanager.core.modules.integration.shipping.dto.ShippingOptionDto;
import com.salesmanager.core.modules.integration.shipping.dto.ShippingQuoteRequestContext;

public final class IntegrationContextMapper {

	private IntegrationContextMapper() {
	}

	public static IntegrationStoreContext toStoreContext(MerchantStore store) {
		IntegrationStoreContext context = new IntegrationStoreContext();
		context.setStoreId(MerchantStoreId.of(store.getCode()));
		if (store.getCurrency() != null) {
			context.setCurrencyCode(store.getCurrency().getCode());
		}
		if (store.getDefaultLanguage() != null) {
			context.setDefaultLanguage(LanguageCode.of(store.getDefaultLanguage().getCode()));
		}
		return context;
	}

	public static PaymentLineItemDto toLineItem(ShoppingCartItem item) {
		PaymentLineItemDto dto = new PaymentLineItemDto();
		dto.setCartItemId(item.getId());
		dto.setSku(item.getSku());
		dto.setQuantity(item.getQuantity());
		dto.setItemPrice(item.getItemPrice());
		dto.setProductId(item.getProductId());
		dto.setVariant(item.getVariant());
		return dto;
	}

	public static List<PaymentLineItemDto> toLineItems(List<ShoppingCartItem> items) {
		List<PaymentLineItemDto> lineItems = new ArrayList<>();
		if (items == null) {
			return lineItems;
		}
		for (ShoppingCartItem item : items) {
			lineItems.add(toLineItem(item));
		}
		return lineItems;
	}

	public static PaymentRequestContext toPaymentRequestContext(MerchantStore store, Customer customer,
			List<ShoppingCartItem> items, BigDecimal amount, Payment payment,
			IntegrationConfiguration configuration, IntegrationModule module) {
		PaymentRequestContext context = new PaymentRequestContext();
		context.setStore(toStoreContext(store));
		if (customer != null) {
			context.setCustomerId(customer.getId());
			context.setCustomerEmail(customer.getEmailAddress());
		}
		context.setLineItems(toLineItems(items));
		context.setAmount(amount);
		if (payment != null) {
			context.setPaymentModuleCode(payment.getModuleName());
			if (payment.getPaymentType() != null) {
				context.setPaymentType(payment.getPaymentType().name());
			}
			if (payment.getTransactionType() != null) {
				context.setTransactionType(payment.getTransactionType().name());
			}
			if (payment.getCurrency() != null) {
				context.setCurrencyCode(payment.getCurrency().getCode());
			}
			if (payment.getPaymentMetaData() != null) {
				context.setPaymentMetaData(new HashMap<>(payment.getPaymentMetaData()));
			}
		}
		context.setConfiguration(configuration);
		context.setModule(module);
		return context;
	}

	public static PaymentCaptureContext toCaptureContext(MerchantStore store, Customer customer, Order order,
			Transaction capturableTransaction, IntegrationConfiguration configuration, IntegrationModule module) {
		PaymentCaptureContext context = new PaymentCaptureContext();
		context.setStore(toStoreContext(store));
		if (customer != null) {
			context.setCustomerId(customer.getId());
			context.setCustomerEmail(customer.getEmailAddress());
		}
		if (order != null) {
			context.setOrderId(order.getId());
		}
		context.setCapturableTransaction(toTransactionResult(capturableTransaction));
		context.setConfiguration(configuration);
		context.setModule(module);
		return context;
	}

	public static PaymentRefundContext toRefundContext(MerchantStore store, Customer customer, Order order,
			boolean partial, Transaction refundableTransaction, BigDecimal amount,
			IntegrationConfiguration configuration, IntegrationModule module) {
		PaymentRefundContext context = new PaymentRefundContext();
		context.setStore(toStoreContext(store));
		if (customer != null) {
			context.setCustomerId(customer.getId());
		}
		if (order != null) {
			context.setOrderId(order.getId());
		}
		context.setPartial(partial);
		context.setAmount(amount);
		context.setRefundableTransaction(toTransactionResult(refundableTransaction));
		context.setConfiguration(configuration);
		context.setModule(module);
		return context;
	}

	public static TransactionResult toTransactionResult(Transaction transaction) {
		if (transaction == null) {
			return null;
		}
		TransactionResult result = new TransactionResult();
		result.setId(transaction.getId());
		result.setAmount(transaction.getAmount());
		result.setTransactionDate(transaction.getTransactionDate());
		if (transaction.getTransactionType() != null) {
			result.setTransactionType(transaction.getTransactionType().name());
		}
		if (transaction.getPaymentType() != null) {
			result.setPaymentType(transaction.getPaymentType().name());
		}
		result.setDetails(transaction.getDetails());
		if (transaction.getTransactionDetails() != null) {
			result.setTransactionDetails(new HashMap<>(transaction.getTransactionDetails()));
		}
		return result;
	}

	public static Transaction toTransaction(TransactionResult result) {
		if (result == null) {
			return null;
		}
		Transaction transaction = new Transaction();
		transaction.setId(result.getId());
		transaction.setAmount(result.getAmount());
		transaction.setTransactionDate(result.getTransactionDate());
		if (result.getTransactionType() != null) {
			transaction.setTransactionType(TransactionType.valueOf(result.getTransactionType()));
		}
		if (result.getPaymentType() != null) {
			transaction.setPaymentType(com.salesmanager.core.model.payments.PaymentType.valueOf(result.getPaymentType()));
		}
		transaction.setDetails(result.getDetails());
		if (result.getTransactionDetails() != null) {
			transaction.setTransactionDetails(new HashMap<>(result.getTransactionDetails()));
		}
		return transaction;
	}

	public static ShippingAddressDto toShippingAddress(Delivery delivery) {
		if (delivery == null) {
			return null;
		}
		ShippingAddressDto dto = new ShippingAddressDto();
		dto.setFirstName(delivery.getFirstName());
		dto.setLastName(delivery.getLastName());
		dto.setCompany(delivery.getCompany());
		dto.setAddress(delivery.getAddress());
		dto.setCity(delivery.getCity());
		dto.setPostalCode(delivery.getPostalCode());
		dto.setState(delivery.getState());
		dto.setTelephone(delivery.getTelephone());
		dto.setLatitude(delivery.getLatitude());
		dto.setLongitude(delivery.getLongitude());
		Country country = delivery.getCountry();
		if (country != null) {
			dto.setCountryCode(country.getIsoCode());
		}
		Zone zone = delivery.getZone();
		if (zone != null) {
			dto.setZoneCode(zone.getCode());
		}
		return dto;
	}

	public static ShippingAddressDto toShippingAddress(ShippingOrigin origin) {
		if (origin == null) {
			return null;
		}
		ShippingAddressDto dto = new ShippingAddressDto();
		dto.setAddress(origin.getAddress());
		dto.setCity(origin.getCity());
		dto.setPostalCode(origin.getPostalCode());
		dto.setState(origin.getState());
		if (origin.getCountry() != null) {
			dto.setCountryCode(origin.getCountry().getIsoCode());
		}
		if (origin.getZone() != null) {
			dto.setZoneCode(origin.getZone().getCode());
		}
		return dto;
	}

	public static PackageDetailsDto toPackageDetails(PackageDetails details) {
		PackageDetailsDto dto = new PackageDetailsDto();
		dto.setCode(details.getCode());
		dto.setShippingWeight(details.getShippingWeight());
		dto.setShippingMaxWeight(details.getShippingMaxWeight());
		dto.setShippingLength(details.getShippingLength());
		dto.setShippingHeight(details.getShippingHeight());
		dto.setShippingWidth(details.getShippingWidth());
		dto.setShippingQuantity(details.getShippingQuantity());
		dto.setTreshold(details.getTreshold());
		dto.setType(details.getType());
		dto.setItemName(details.getItemName());
		return dto;
	}

	public static List<PackageDetailsDto> toPackageDetails(List<PackageDetails> packages) {
		List<PackageDetailsDto> dtos = new ArrayList<>();
		if (packages == null) {
			return dtos;
		}
		for (PackageDetails details : packages) {
			dtos.add(toPackageDetails(details));
		}
		return dtos;
	}

	public static ShippingQuoteRequestContext toShippingQuoteRequestContext(MerchantStore store,
			List<PackageDetails> packages, BigDecimal orderTotal, Delivery delivery, ShippingOrigin origin,
			IntegrationConfiguration configuration, IntegrationModule module, Locale locale) {
		ShippingQuoteRequestContext context = new ShippingQuoteRequestContext();
		context.setStore(toStoreContext(store));
		context.setPackages(toPackageDetails(packages));
		context.setOrderTotal(orderTotal);
		context.setDelivery(toShippingAddress(delivery));
		context.setOrigin(toShippingAddress(origin));
		if (locale != null) {
			context.setLocale(locale.toString());
		}
		context.setConfiguration(configuration);
		context.setModule(module);
		return context;
	}

	public static ShippingOption toShippingOption(ShippingOptionDto dto) {
		if (dto == null) {
			return null;
		}
		ShippingOption option = new ShippingOption();
		option.setOptionPrice(dto.getOptionPrice());
		option.setOptionName(dto.getOptionName());
		option.setOptionCode(dto.getOptionCode());
		option.setOptionDeliveryDate(dto.getOptionDeliveryDate());
		option.setOptionShippingDate(dto.getOptionShippingDate());
		option.setOptionPriceText(dto.getOptionPriceText());
		option.setOptionId(dto.getOptionId());
		option.setDescription(dto.getDescription());
		option.setShippingModuleCode(dto.getShippingModuleCode());
		option.setNote(dto.getNote());
		option.setEstimatedNumberOfDays(dto.getEstimatedNumberOfDays());
		return option;
	}

	public static ShippingOptionDto toShippingOptionDto(ShippingOption option) {
		if (option == null) {
			return null;
		}
		ShippingOptionDto dto = new ShippingOptionDto();
		dto.setOptionPrice(option.getOptionPrice());
		dto.setOptionName(option.getOptionName());
		dto.setOptionCode(option.getOptionCode());
		dto.setOptionDeliveryDate(option.getOptionDeliveryDate());
		dto.setOptionShippingDate(option.getOptionShippingDate());
		dto.setOptionPriceText(option.getOptionPriceText());
		dto.setOptionId(option.getOptionId());
		dto.setDescription(option.getDescription());
		dto.setShippingModuleCode(option.getShippingModuleCode());
		dto.setNote(option.getNote());
		dto.setEstimatedNumberOfDays(option.getEstimatedNumberOfDays());
		return dto;
	}

	public static List<ShippingOption> toShippingOptions(List<ShippingOptionDto> dtos) {
		List<ShippingOption> options = new ArrayList<>();
		if (dtos == null) {
			return options;
		}
		for (ShippingOptionDto dto : dtos) {
			options.add(toShippingOption(dto));
		}
		return options;
	}

	public static List<ShippingOptionDto> toShippingOptionDtos(List<ShippingOption> options) {
		List<ShippingOptionDto> dtos = new ArrayList<>();
		if (options == null) {
			return dtos;
		}
		for (ShippingOption option : options) {
			dtos.add(toShippingOptionDto(option));
		}
		return dtos;
	}

}

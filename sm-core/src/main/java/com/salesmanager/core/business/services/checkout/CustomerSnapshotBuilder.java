package com.salesmanager.core.business.services.checkout;

import com.salesmanager.contracts.customer.AddressSnapshot;
import com.salesmanager.contracts.customer.CustomerSnapshot;
import com.salesmanager.core.model.common.Billing;
import com.salesmanager.core.model.common.Delivery;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;

public final class CustomerSnapshotBuilder {

	private CustomerSnapshotBuilder() {
	}

	public static CustomerSnapshot from(Customer customer) {
		if (customer == null) {
			CustomerSnapshot anonymous = new CustomerSnapshot();
			anonymous.setAnonymous(true);
			return anonymous;
		}

		CustomerSnapshot snapshot = new CustomerSnapshot();
		snapshot.setId(customer.getId());
		snapshot.setEmailAddress(customer.getEmailAddress());
		snapshot.setNick(customer.getNick());
		snapshot.setCompany(customer.getCompany());
		snapshot.setAnonymous(customer.isAnonymous());
		snapshot.setBilling(fromBilling(customer.getBilling()));
		snapshot.setDelivery(fromDelivery(customer.getDelivery()));

		Language language = customer.getDefaultLanguage();
		if (language != null) {
			snapshot.setLanguage(language.getCode());
		}

		return snapshot;
	}

	static AddressSnapshot fromBilling(Billing billing) {
		if (billing == null) {
			return null;
		}
		AddressSnapshot address = new AddressSnapshot();
		address.setFirstName(billing.getFirstName());
		address.setLastName(billing.getLastName());
		address.setCompany(billing.getCompany());
		address.setAddress(billing.getAddress());
		address.setCity(billing.getCity());
		address.setPostalCode(billing.getPostalCode());
		address.setState(billing.getState());
		address.setTelephone(billing.getTelephone());
		address.setCountryCode(countryCode(billing.getCountry()));
		address.setZoneCode(zoneCode(billing.getZone()));
		return address;
	}

	static AddressSnapshot fromDelivery(Delivery delivery) {
		if (delivery == null) {
			return null;
		}
		AddressSnapshot address = new AddressSnapshot();
		address.setFirstName(delivery.getFirstName());
		address.setLastName(delivery.getLastName());
		address.setCompany(delivery.getCompany());
		address.setAddress(delivery.getAddress());
		address.setCity(delivery.getCity());
		address.setPostalCode(delivery.getPostalCode());
		address.setState(delivery.getState());
		address.setTelephone(delivery.getTelephone());
		address.setCountryCode(countryCode(delivery.getCountry()));
		address.setZoneCode(zoneCode(delivery.getZone()));
		return address;
	}

	private static String countryCode(Country country) {
		return country != null ? country.getIsoCode() : null;
	}

	private static String zoneCode(Zone zone) {
		return zone != null ? zone.getCode() : null;
	}

}

package com.salesmanager.core.business.services.checkout;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.customer.CustomerSnapshot;
import com.salesmanager.core.model.common.Billing;
import com.salesmanager.core.model.common.Delivery;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;

class CustomerSnapshotBuilderTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void fromRegisteredCustomerMapsAddressesWithoutLazyCollections() throws Exception {
		Customer customer = registeredCustomer();

		CustomerSnapshot snapshot = CustomerSnapshotBuilder.from(customer);
		JsonNode tree = mapper.readTree(mapper.writeValueAsString(snapshot));

		assertThat(snapshot.getId()).isEqualTo(12L);
		assertThat(snapshot.getEmailAddress()).isEqualTo("buyer@example.com");
		assertThat(snapshot.isAnonymous()).isFalse();
		assertThat(snapshot.getLanguageCode()).isEqualTo("en");
		assertThat(snapshot.getBilling().getCountryCode()).isEqualTo("US");
		assertThat(snapshot.getDelivery().getZoneCode()).isEqualTo("NY");
		assertThat(tree.has("attributes")).isFalse();
		assertThat(tree.has("reviews")).isFalse();
		assertThat(tree.has("password")).isFalse();
	}

	@Test
	void fromNullCustomerReturnsAnonymousSnapshot() {
		CustomerSnapshot snapshot = CustomerSnapshotBuilder.from(null);

		assertThat(snapshot.isAnonymous()).isTrue();
		assertThat(snapshot.getId()).isNull();
		assertThat(snapshot.getEmailAddress()).isNull();
	}

	@Test
	void fromAnonymousCustomerKeepsEmailOnly() {
		Customer customer = new Customer();
		customer.setAnonymous(true);
		customer.setEmailAddress("guest@example.com");

		CustomerSnapshot snapshot = CustomerSnapshotBuilder.from(customer);

		assertThat(snapshot.isAnonymous()).isTrue();
		assertThat(snapshot.getEmailAddress()).isEqualTo("guest@example.com");
		assertThat(snapshot.getBilling()).isNull();
	}

	private static Customer registeredCustomer() {
		Country country = new Country();
		country.setIsoCode("US");

		Zone zone = new Zone();
		zone.setCode("NY");

		Billing billing = new Billing();
		billing.setFirstName("Jane");
		billing.setLastName("Doe");
		billing.setCity("New York");
		billing.setCountry(country);

		Delivery delivery = new Delivery();
		delivery.setFirstName("Jane");
		delivery.setLastName("Doe");
		delivery.setCity("New York");
		delivery.setCountry(country);
		delivery.setZone(zone);

		Customer customer = new Customer();
		customer.setId(12L);
		customer.setEmailAddress("buyer@example.com");
		customer.setNick("jane");
		customer.setBilling(billing);
		customer.setDelivery(delivery);
		customer.setDefaultLanguage(new Language("en"));

		return customer;
	}

}

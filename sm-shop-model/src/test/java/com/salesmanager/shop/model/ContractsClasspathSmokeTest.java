package com.salesmanager.shop.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Smoke: shopizer-api-contracts types are on the sm-shop-model classpath,
 * and legacy shop-model aliases remain loadable (and marked {@link Deprecated}).
 */
class ContractsClasspathSmokeTest {

	@Test
	void contractsReadableCountryIsLoadable() throws Exception {
		Class<?> contractsType = Class.forName("com.salesmanager.contracts.reference.ReadableCountry");
		assertNotNull(contractsType.getDeclaredConstructor().newInstance());
	}

	@Test
	void contractsTypesVisibleAlongsideLegacyAliases() throws Exception {
		Class<?> contractsCountry = Class.forName("com.salesmanager.contracts.reference.ReadableCountry");
		Class<?> legacyCountry = Class.forName("com.salesmanager.shop.model.references.ReadableCountry");
		Class<?> contractsTaxClass = Class.forName("com.salesmanager.contracts.tax.ReadableTaxClass");
		Class<?> legacyTaxClass = Class.forName("com.salesmanager.shop.model.tax.ReadableTaxClass");
		Class<?> contractsEntity = Class.forName("com.salesmanager.contracts.common.Entity");
		Class<?> legacyEntity = Class.forName("com.salesmanager.shop.model.entity.Entity");

		assertNotNull(contractsCountry);
		assertNotNull(contractsTaxClass);
		assertNotNull(contractsEntity);
		assertTrue(legacyCountry.isAnnotationPresent(Deprecated.class));
		assertTrue(legacyTaxClass.isAnnotationPresent(Deprecated.class));
		assertTrue(legacyEntity.isAnnotationPresent(Deprecated.class));
	}
}

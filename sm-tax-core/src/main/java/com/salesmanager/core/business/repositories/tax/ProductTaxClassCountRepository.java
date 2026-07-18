package com.salesmanager.core.business.repositories.tax;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.salesmanager.core.model.catalog.product.Product;

/**
 * Minimal PRODUCT count for TaxClass delete guard (shared DB; ADR-002).
 * Kept here so {@code sm-tax-core} does not depend on catalog repos in {@code sm-core}.
 */
public interface ProductTaxClassCountRepository extends JpaRepository<Product, Long> {

	@Query("select count(p) from Product p where p.taxClass.id = ?1")
	long countByTaxClassId(Long taxClassId);
}

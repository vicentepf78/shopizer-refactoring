package com.salesmanager.tax.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.salesmanager.core.model.merchant.MerchantStore;

public interface MerchantStoreRepository extends JpaRepository<MerchantStore, Integer> {

	@Query("select m from MerchantStore m "
			+ "left join fetch m.parent "
			+ "left join fetch m.defaultLanguage "
			+ "where m.code = ?1")
	MerchantStore findByCode(String code);
}

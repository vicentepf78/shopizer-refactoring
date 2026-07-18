package com.salesmanager.tax.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.tax.support.ResourceNotFoundException;
import com.salesmanager.tax.support.StoreForbiddenException;

@Service
public class StoreAuthorizationService {

	public void authorize(MerchantStore store, String requestUri) {
		if (store == null) {
			throw new ResourceNotFoundException("MerchantStore is not found");
		}
		if (requestUri == null || !requestUri.contains("/private")) {
			return;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof JWTUser)) {
			throw new StoreForbiddenException("Cannot authorize user for store " + store.getCode());
		}

		JWTUser user = (JWTUser) authentication.getPrincipal();
		if (store.getCode() != null && store.getCode().equalsIgnoreCase(user.getStoreCode())) {
			return;
		}

		for (GrantedAuthority authority : user.getAuthorities()) {
			if (JWTAdminDetailsService.GROUP_SUPERADMIN.equals(authority.getAuthority())) {
				return;
			}
		}

		throw new StoreForbiddenException("Cannot authorize user for store " + store.getCode());
	}
}

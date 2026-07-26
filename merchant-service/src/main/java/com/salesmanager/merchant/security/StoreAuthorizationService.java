package com.salesmanager.merchant.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.merchant.support.MerchantConstants;
import com.salesmanager.merchant.support.ResourceNotFoundException;
import com.salesmanager.merchant.support.UnauthorizedException;

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
			throw new UnauthorizedException("Cannot authorize user for store " + store.getCode());
		}

		JWTUser user = (JWTUser) authentication.getPrincipal();
		if (store.getCode() != null && store.getCode().equalsIgnoreCase(user.getStoreCode())) {
			return;
		}

		for (GrantedAuthority authority : user.getAuthorities()) {
			if (MerchantConstants.GROUP_SUPERADMIN.equals(authority.getAuthority())) {
				return;
			}
		}

		throw new UnauthorizedException("Cannot authorize user for store " + store.getCode());
	}
}

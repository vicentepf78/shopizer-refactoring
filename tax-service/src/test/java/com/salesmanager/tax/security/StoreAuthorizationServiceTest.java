package com.salesmanager.tax.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.tax.support.StoreForbiddenException;

class StoreAuthorizationServiceTest {

	private final StoreAuthorizationService service = new StoreAuthorizationService();

	@AfterEach
	void clear() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void authorize_matchingStore_ok() {
		JWTUser user = new JWTUser(1L, "admin", "A", "B", "a@b.c", "x", "DEFAULT",
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_AUTH")), true, null);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		assertThatCode(() -> service.authorize(store, "/api/v1/private/tax/class")).doesNotThrowAnyException();
	}

	@Test
	void authorize_mismatchWithoutSuperAdmin_forbidden() {
		JWTUser user = new JWTUser(1L, "admin", "A", "B", "a@b.c", "x", "DEFAULT",
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_AUTH")), true, null);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

		MerchantStore store = new MerchantStore();
		store.setCode("OTHER");
		assertThatThrownBy(() -> service.authorize(store, "/api/v1/private/tax/class"))
				.isInstanceOf(StoreForbiddenException.class);
	}

	@Test
	void authorize_superAdmin_okForOtherStore() {
		JWTUser user = new JWTUser(1L, "admin", "A", "B", "a@b.c", "x", "DEFAULT",
				java.util.Arrays.asList(
						new SimpleGrantedAuthority("ROLE_AUTH"),
						new SimpleGrantedAuthority(JWTAdminDetailsService.GROUP_SUPERADMIN)),
				true, null);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

		MerchantStore store = new MerchantStore();
		store.setCode("OTHER");
		assertThatCode(() -> service.authorize(store, "/api/v1/private/tax/class")).doesNotThrowAnyException();
	}
}

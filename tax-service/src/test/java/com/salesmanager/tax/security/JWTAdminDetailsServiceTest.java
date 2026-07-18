package com.salesmanager.tax.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.User;

@ExtendWith(MockitoExtension.class)
class JWTAdminDetailsServiceTest {

	@Mock
	private AdminUserRepository adminUserRepository;

	@Test
	void loadUserByUsername_mapsAuthoritiesAndStore() {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Group group = new Group(JWTAdminDetailsService.GROUP_SUPERADMIN);
		User user = new User("admin", "pwd", "a@b.c");
		user.setId(1L);
		user.setFirstName("A");
		user.setLastName("B");
		user.setMerchantStore(store);
		user.setGroups(Collections.singletonList(group));

		when(adminUserRepository.findByUserName("admin")).thenReturn(user);

		UserDetails details = new JWTAdminDetailsService(adminUserRepository).loadUserByUsername("admin");
		assertThat(details.getUsername()).isEqualTo("admin");
		assertThat(details.getAuthorities()).extracting("authority")
				.contains("ROLE_AUTH", JWTAdminDetailsService.GROUP_SUPERADMIN);
		assertThat(((JWTUser) details).getStoreCode()).isEqualTo("DEFAULT");
	}

	@Test
	void loadUserByUsername_missing_throws() {
		when(adminUserRepository.findByUserName("x")).thenReturn(null);
		assertThatThrownBy(() -> new JWTAdminDetailsService(adminUserRepository).loadUserByUsername("x"))
				.isInstanceOf(UsernameNotFoundException.class);
	}
}

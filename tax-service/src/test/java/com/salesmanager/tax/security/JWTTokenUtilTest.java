package com.salesmanager.tax.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

class JWTTokenUtilTest {

	private JWTTokenUtil util;

	@BeforeEach
	void setUp() {
		util = new JWTTokenUtil();
		ReflectionTestUtils.setField(util, "secret", "aSecret");
		ReflectionTestUtils.setField(util, "expiration", 3600L);
	}

	@Test
	void generateAndValidateToken() {
		JWTUser user = new JWTUser(1L, "admin", "A", "B", "a@b.c", "pwd", "DEFAULT",
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_AUTH")), true, null);

		String token = util.generateToken(user);
		assertThat(util.getUsernameFromToken(token)).isEqualTo("admin");
		assertThat(util.getExpirationDateFromToken(token)).isAfter(new Date());
		assertThat(util.validateToken(token, user)).isTrue();
	}

	@Test
	void generateToken_byUsername() {
		String token = util.generateToken("admin");
		assertThat(util.getUsernameFromToken(token)).isEqualTo("admin");
		assertThat(util.getIssuedAtDateFromToken(token)).isNotNull();
	}
}

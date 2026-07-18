package com.salesmanager.tax.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthenticationTokenFilterTest {

	@Mock
	private JWTTokenUtil jwtTokenUtil;
	@Mock
	private UserDetailsService userDetailsService;
	@Mock
	private FilterChain chain;

	private AuthenticationTokenFilter filter;

	@BeforeEach
	void setUp() {
		filter = new AuthenticationTokenFilter(jwtTokenUtil, userDetailsService);
		ReflectionTestUtils.setField(filter, "tokenHeader", "Authorization");
	}

	@AfterEach
	void clear() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void privatePath_withValidBearer_setsAuthentication() throws Exception {
		JWTUser user = new JWTUser(1L, "admin", "A", "B", "a@b.c", "p", "DEFAULT",
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_AUTH")), true, null);
		when(jwtTokenUtil.getUsernameFromToken("tok")).thenReturn("admin");
		when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);
		when(jwtTokenUtil.validateToken("tok", user)).thenReturn(true);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/private/tax/class");
		request.setRequestURI("/api/v1/private/tax/class");
		// RequestURL must contain /api/v1/private
		request.setScheme("http");
		request.setServerName("localhost");
		request.setRequestURI("/api/v1/private/tax/class");
		request.addHeader("Authorization", "Bearer tok");

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin");
	}

	@Test
	void privatePath_withoutToken_leavesUnauthenticated() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setScheme("http");
		request.setServerName("localhost");
		request.setRequestURI("/api/v1/private/tax/class");

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	void invalidToken_doesNotSetAuthentication() throws Exception {
		when(jwtTokenUtil.getUsernameFromToken(anyString())).thenThrow(new IllegalArgumentException("bad"));

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setScheme("http");
		request.setServerName("localhost");
		request.setRequestURI("/api/v1/private/tax/class");
		request.addHeader("Authorization", "Bearer bad");

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}
}

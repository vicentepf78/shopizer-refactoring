package com.salesmanager.tax.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class TaxSecurityConfig {

	@Configuration
	@Order(1)
	public static class PrivateApiSecurity extends WebSecurityConfigurerAdapter {

		private final AuthenticationTokenFilter authenticationTokenFilter;
		private final UserDetailsService jwtAdminDetailsService;

		public PrivateApiSecurity(
				AuthenticationTokenFilter authenticationTokenFilter,
				UserDetailsService jwtAdminDetailsService) {
			this.authenticationTokenFilter = authenticationTokenFilter;
			this.jwtAdminDetailsService = jwtAdminDetailsService;
		}

		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(jwtAdminDetailsService);
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.antMatcher("/api/v*/private/**")
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
					.and()
					.authorizeRequests()
					.antMatchers(HttpMethod.OPTIONS, "/api/v*/private/**").permitAll()
					.anyRequest().hasRole(JWTAdminDetailsService.ROLE_AUTH)
					.and()
					.exceptionHandling().authenticationEntryPoint(apiAdminAuthenticationEntryPoint())
					.and()
					.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
					.csrf().disable()
					.httpBasic().disable();
		}

		@Bean
		public AuthenticationEntryPoint apiAdminAuthenticationEntryPoint() {
			BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
			entryPoint.setRealmName("api-admin-realm");
			return entryPoint;
		}
	}

	@Configuration
	@Order(2)
	public static class PublicAndActuatorSecurity extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.authorizeRequests()
					.antMatchers("/actuator/**").permitAll()
					.anyRequest().permitAll()
					.and()
					.csrf().disable();
		}
	}
}

package com.salesmanager.tax.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;

@Component
public class AuthenticationTokenFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenFilter.class);
	private static final String BEARER_TOKEN = "Bearer ";

	@Value("${authToken.header}")
	private String tokenHeader;

	private final JWTTokenUtil jwtTokenUtil;
	private final UserDetailsService jwtAdminDetailsService;

	public AuthenticationTokenFilter(JWTTokenUtil jwtTokenUtil, UserDetailsService jwtAdminDetailsService) {
		this.jwtTokenUtil = jwtTokenUtil;
		this.jwtAdminDetailsService = jwtAdminDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String requestUrl = request.getRequestURL().toString();
		if (requestUrl.contains("/api/v1/private") || requestUrl.contains("/api/v2/private")) {
			final String requestHeader = request.getHeader(this.tokenHeader);
			if (requestHeader != null && requestHeader.startsWith(BEARER_TOKEN)) {
				String authToken = requestHeader.substring(BEARER_TOKEN.length()).trim();
				try {
					String username = jwtTokenUtil.getUsernameFromToken(authToken);
					if (StringUtils.isNotBlank(username)
							&& SecurityContextHolder.getContext().getAuthentication() == null) {
						UserDetails userDetails = jwtAdminDetailsService.loadUserByUsername(username);
						if (jwtTokenUtil.validateToken(authToken, userDetails)) {
							UsernamePasswordAuthenticationToken authentication =
									new UsernamePasswordAuthenticationToken(
											userDetails, null, userDetails.getAuthorities());
							authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
							SecurityContextHolder.getContext().setAuthentication(authentication);
						}
					}
				} catch (ExpiredJwtException e) {
					LOGGER.warn("JWT expired", e);
				} catch (Exception e) {
					LOGGER.warn("JWT authentication failed", e);
				}
			}
		}

		chain.doFilter(request, response);
	}
}

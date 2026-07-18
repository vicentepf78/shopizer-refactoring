package com.salesmanager.tax.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.User;

@Service("jwtAdminDetailsService")
public class JWTAdminDetailsService implements UserDetailsService {

	public static final String ROLE_PREFIX = "ROLE_";
	public static final String ROLE_AUTH = "AUTH";
	public static final String GROUP_SUPERADMIN = "SUPERADMIN";

	private final AdminUserRepository adminUserRepository;

	public JWTAdminDetailsService(AdminUserRepository adminUserRepository) {
		this.adminUserRepository = adminUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		User user = adminUserRepository.findByUserName(userName);
		if (user == null) {
			throw new UsernameNotFoundException("User " + userName + " not found");
		}

		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + ROLE_AUTH));

		List<Group> groups = user.getGroups();
		if (groups != null) {
			for (Group group : groups) {
				if (group.getGroupName() != null) {
					authorities.add(new SimpleGrantedAuthority(group.getGroupName()));
				}
			}
		}

		String storeCode = user.getMerchantStore() != null ? user.getMerchantStore().getCode() : null;
		return new JWTUser(
				user.getId(),
				userName,
				user.getFirstName(),
				user.getLastName(),
				user.getAdminEmail(),
				user.getAdminPassword(),
				storeCode,
				authorities,
				true,
				null);
	}
}

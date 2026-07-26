package com.salesmanager.merchant.security;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.user.User;
import com.salesmanager.merchant.support.MerchantConstants;
import com.salesmanager.merchant.support.ServiceRuntimeException;
import com.salesmanager.merchant.support.UnauthorizedException;

@Service
public class UserAuthorizationService {

	private final AdminUserRepository adminUserRepository;
	private final MerchantStoreService merchantStoreService;

	public UserAuthorizationService(
			AdminUserRepository adminUserRepository,
			MerchantStoreService merchantStoreService) {
		this.adminUserRepository = adminUserRepository;
		this.merchantStoreService = merchantStoreService;
	}

	public String authenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			throw new UnauthorizedException();
		}
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			return authentication.getName();
		}
		throw new UnauthorizedException();
	}

	public void authorizedGroup(String userName, List<String> groupNames) {
		User user = adminUserRepository.findByUserName(userName);
		if (user == null) {
			throw new UnauthorizedException("User " + userName + " not authorized");
		}
		Set<String> groups = user.getGroups().stream()
				.map(g -> g.getGroupName())
				.collect(Collectors.toSet());
		for (String groupName : groupNames) {
			if (groups.contains(groupName)) {
				return;
			}
		}
		throw new UnauthorizedException("User " + userName + " not authorized");
	}

	public boolean userInRoles(String userName, List<String> groupNames) {
		User user = adminUserRepository.findByUserName(userName);
		if (user == null) {
			return false;
		}
		Set<String> groups = user.getGroups().stream()
				.map(g -> g.getGroupName())
				.collect(Collectors.toSet());
		return groupNames.stream().anyMatch(groups::contains);
	}

	public boolean authorizedStore(String userName, String merchantStoreCode) {
		try {
			User user = adminUserRepository.findByUserName(userName);
			if (user == null) {
				return false;
			}
			if (userInRoles(userName, List.of(MerchantConstants.GROUP_SUPERADMIN))) {
				return true;
			}
			MerchantStore userStore = user.getMerchantStore();
			if (userStore != null && merchantStoreCode.equalsIgnoreCase(userStore.getCode())) {
				return true;
			}
			MerchantStore parent = merchantStoreService.getParent(merchantStoreCode);
			return parent != null
					&& userStore != null
					&& parent.getCode().equalsIgnoreCase(userStore.getCode());
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e.getMessage(), e);
		}
	}

	public void requireStoreAccess(String userName, String storeCode) {
		if (!authorizedStore(userName, storeCode)) {
			throw new UnauthorizedException("User " + userName + " not authorized");
		}
	}
}

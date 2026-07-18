package com.salesmanager.tax.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.salesmanager.core.model.user.User;

public interface AdminUserRepository extends JpaRepository<User, Long> {

	@Query("select distinct u from User as u "
			+ "left join fetch u.groups ug "
			+ "join fetch u.merchantStore um "
			+ "left join fetch u.defaultLanguage ul "
			+ "where u.adminName = ?1")
	User findByUserName(String userName);
}

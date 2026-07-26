package com.salesmanager.merchant.api.v1.store;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.salesmanager.contracts.common.EntityExists;
import com.salesmanager.contracts.merchant.PersistableBrand;
import com.salesmanager.contracts.merchant.PersistableMerchantStore;
import com.salesmanager.contracts.merchant.ReadableBrand;
import com.salesmanager.contracts.merchant.ReadableMerchantStore;
import com.salesmanager.contracts.merchant.ReadableMerchantStoreList;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.merchant.MerchantStoreCriteria;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.merchant.facade.StoreFacade;
import com.salesmanager.merchant.security.UserAuthorizationService;
import com.salesmanager.merchant.support.MerchantConstants;
import com.salesmanager.merchant.support.RestApiException;
import com.salesmanager.merchant.support.ServiceRequestCriteriaBuilderUtils;
import com.salesmanager.merchant.support.UnauthorizedException;

@RestController
@RequestMapping("/api/v1")
public class MerchantStoreController {

	private static final Map<String, String> MAPPING_FIELDS = Map.of(
			"name", "name",
			"readableAudit.user", "auditSection.modifiedBy");

	private final StoreFacade storeFacade;
	private final UserAuthorizationService userAuthorizationService;

	public MerchantStoreController(StoreFacade storeFacade, UserAuthorizationService userAuthorizationService) {
		this.storeFacade = storeFacade;
		this.userAuthorizationService = userAuthorizationService;
	}

	@GetMapping(value = { "/store/{code}" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReadableMerchantStore store(
			@PathVariable String code,
			@RequestParam(value = "lang", required = false) String lang) {
		return storeFacade.getByCode(code, lang);
	}

	@GetMapping(value = { "/private/store/{code}" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReadableMerchantStore storeFull(@PathVariable String code, Language language) {
		String authenticatedUser = userAuthorizationService.authenticatedUser();
		userAuthorizationService.authorizedGroup(
				authenticatedUser, Stream.of("SUPERADMIN", "ADMIN_RETAILER").collect(Collectors.toList()));
		return storeFacade.getFullByCode(code, language);
	}

	@GetMapping(value = { "/private/merchant/{code}/stores" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReadableMerchantStoreList list(
			@PathVariable String code,
			Language language,
			@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
			@RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {
		String authenticatedUser = userAuthorizationService.authenticatedUser();
		userAuthorizationService.authorizedGroup(
				authenticatedUser, Stream.of("SUPERADMIN", "ADMIN_RETAILER").collect(Collectors.toList()));
		return storeFacade.getChildStores(language, code, page, count);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/stores" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReadableMerchantStoreList get(
			MerchantStore merchantStore,
			Language language,
			@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
			@RequestParam(value = "count", required = false, defaultValue = "10") Integer count,
			HttpServletRequest request) {
		String authenticatedUser = userAuthorizationService.authenticatedUser();
		userAuthorizationService.authorizedGroup(authenticatedUser, Stream.of(
				MerchantConstants.GROUP_SUPERADMIN,
				MerchantConstants.GROUP_ADMIN,
				MerchantConstants.GROUP_ADMIN_RETAIL).collect(Collectors.toList()));

		MerchantStoreCriteria criteria = ServiceRequestCriteriaBuilderUtils.createMerchantStoreCriteria(
				MAPPING_FIELDS, request);
		if (userAuthorizationService.userInRoles(authenticatedUser, Arrays.asList(MerchantConstants.GROUP_SUPERADMIN))) {
			criteria.setStoreCode(null);
		} else {
			criteria.setStoreCode(merchantStore.getCode());
		}
		return storeFacade.findAll(criteria, language, page, count);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/stores/names" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ReadableMerchantStore> listNames(
			MerchantStore merchantStore,
			Language language,
			@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
			@RequestParam(value = "count", required = false, defaultValue = "10") Integer count,
			HttpServletRequest request) {
		String authenticatedUser = userAuthorizationService.authenticatedUser();
		userAuthorizationService.authorizedGroup(authenticatedUser, Stream.of(
				MerchantConstants.GROUP_SUPERADMIN,
				MerchantConstants.GROUP_ADMIN,
				MerchantConstants.GROUP_ADMIN_RETAIL).collect(Collectors.toList()));

		MerchantStoreCriteria criteria = ServiceRequestCriteriaBuilderUtils.createMerchantStoreCriteria(
				MAPPING_FIELDS, request);
		if (userAuthorizationService.userInRoles(authenticatedUser, Arrays.asList(MerchantConstants.GROUP_SUPERADMIN))) {
			criteria.setStoreCode(null);
		} else {
			criteria.setStoreCode(merchantStore.getCode());
		}
		ReadableMerchantStoreList list = storeFacade.findAll(criteria, language, page, count);
		return list.getData();
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/store/languages" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Language> supportedLanguages(MerchantStore merchantStore) {
		return storeFacade.supportedLanguages(merchantStore);
	}

	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = { "/private/store" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public void create(@Valid @RequestBody PersistableMerchantStore store) {
		String authenticatedUser = userAuthorizationService.authenticatedUser();
		userAuthorizationService.authorizedGroup(
				authenticatedUser, Stream.of("SUPERADMIN", "ADMIN_RETAILER").collect(Collectors.toList()));
		storeFacade.create(store);
	}

	@ResponseStatus(HttpStatus.OK)
	@PutMapping(value = { "/private/store/{code}" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public void update(
			@PathVariable String code,
			@Valid @RequestBody PersistableMerchantStore store,
			HttpServletRequest request) {
		String userName = getUserFromRequest(request);
		userAuthorizationService.requireStoreAccess(userName, code);
		store.setCode(code);
		storeFacade.update(store);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/store/{code}/marketing" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReadableBrand getStoreMarketing(@PathVariable String code, HttpServletRequest request) {
		String userName = getUserFromRequest(request);
		userAuthorizationService.requireStoreAccess(userName, code);
		return storeFacade.getBrand(code);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/merchant/{code}/children" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReadableMerchantStoreList children(
			@PathVariable String code,
			Language language,
			@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
			@RequestParam(value = "count", required = false, defaultValue = "10") Integer count,
			HttpServletRequest request) {
		String userName = getUserFromRequest(request);
		userAuthorizationService.requireStoreAccess(userName, code);
		return storeFacade.getChildStores(language, code, page, count);
	}

	@Deprecated
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(value = { "/private/store/{code}/marketing" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public void saveStoreMarketing(
			@PathVariable String code,
			@RequestBody PersistableBrand brand,
			HttpServletRequest request) {
		String userName = getUserFromRequest(request);
		userAuthorizationService.requireStoreAccess(userName, code);
		storeFacade.createBrand(code, brand);
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(value = { "/private/store/{code}/marketing/logo" })
	public void addLogo(
			@PathVariable String code,
			@RequestParam("file") MultipartFile uploadfile,
			HttpServletRequest request) {
		String userName = getUserFromRequest(request);
		userAuthorizationService.requireStoreAccess(userName, code);
		if (uploadfile.isEmpty()) {
			throw new RestApiException("Upload file is empty");
		}
		try {
			byte[] bytes = uploadfile.getBytes();
			storeFacade.addStoreLogo(code, uploadfile.getOriginalFilename(), bytes, uploadfile.getContentType());
		} catch (IOException ioe) {
			throw new RestApiException(ioe);
		}
	}

	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping(value = { "/private/store/{code}/marketing/logo" })
	public void deleteStoreLogo(@PathVariable String code, HttpServletRequest request) {
		String userName = getUserFromRequest(request);
		userAuthorizationService.requireStoreAccess(userName, code);
		storeFacade.deleteLogo(code);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/store/unique", "/private/store/unique" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code) {
		return new ResponseEntity<>(new EntityExists(storeFacade.existByCode(code)), HttpStatus.OK);
	}

	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping(value = { "/private/store/{code}" })
	public void delete(@PathVariable String code, HttpServletRequest request) {
		String userName = getUserFromRequest(request);
		userAuthorizationService.requireStoreAccess(userName, code);
		storeFacade.delete(code);
	}

	private String getUserFromRequest(HttpServletRequest request) {
		Principal principal = request.getUserPrincipal();
		if (principal == null) {
			throw new UnauthorizedException();
		}
		return principal.getName();
	}
}

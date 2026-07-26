package com.salesmanager.core.business.services.merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.merchant.MerchantRepository;
import com.salesmanager.core.business.repositories.merchant.PageableMerchantRepository;
import com.salesmanager.core.model.common.GenericEntityList;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.merchant.MerchantStoreCriteria;

@ExtendWith(MockitoExtension.class)
class MerchantStoreServiceImplTest {

	@Mock
	private MerchantRepository merchantRepository;

	@Mock
	private PageableMerchantRepository pageableMerchantRepository;

	private MerchantStoreServiceImpl merchantStoreService;

	@BeforeEach
	void setUp() {
		merchantStoreService = new MerchantStoreServiceImpl(merchantRepository);
		injectPageableRepository();
	}

	@Test
	void doesNotReferenceProductTypeService() {
		List<String> fieldTypes = Arrays.stream(MerchantStoreServiceImpl.class.getDeclaredFields())
				.map(Field::getType)
				.map(Class::getName)
				.collect(Collectors.toList());

		assertFalse(fieldTypes.stream().anyMatch(name -> name.contains("ProductType")),
				"MerchantStoreServiceImpl must not depend on ProductType");
	}

	@Test
	void defaultStoreCode_isProtectedConstant() {
		assertEquals("DEFAULT", MerchantStore.DEFAULT_STORE);
	}

	@Test
	void getByCode_delegatesToRepository() throws Exception {
		MerchantStore store = store(1, "ALPHA");
		when(merchantRepository.findByCode("ALPHA")).thenReturn(store);

		assertSame(store, merchantStoreService.getByCode("ALPHA"));
	}

	@Test
	void existByCode_delegatesToRepository() {
		when(merchantRepository.existsByCode("ALPHA")).thenReturn(true);

		assertTrue(merchantStoreService.existByCode("ALPHA"));
	}

	@Test
	void getParent_nullCode_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> merchantStoreService.getParent(null));
	}

	@Test
	void getParent_missingStore_throwsServiceException() {
		when(merchantRepository.findByCode("MISSING")).thenReturn(null);

		assertThrows(ServiceException.class, () -> merchantStoreService.getParent("MISSING"));
	}

	@Test
	void getParent_withoutParent_returnsSelf() throws Exception {
		MerchantStore store = store(1, "RETAIL");
		store.setRetailer(true);
		when(merchantRepository.findByCode("RETAIL")).thenReturn(store);

		assertSame(store, merchantStoreService.getParent("RETAIL"));
	}

	@Test
	void listAllRetailers_delegatesToRepository() throws Exception {
		Page<MerchantStore> page = new PageImpl<>(Collections.emptyList());
		when(pageableMerchantRepository.listAllRetailers(eq("Retail"), any(PageRequest.class))).thenReturn(page);

		merchantStoreService.listAllRetailers(Optional.of("Retail"), 0, 5);

		verify(pageableMerchantRepository).listAllRetailers(eq("Retail"), any(PageRequest.class));
	}

	@Test
	void findAllStoreNames_delegatesToRepository() throws Exception {
		List<MerchantStore> stores = Collections.singletonList(store(1, "N1"));
		when(merchantRepository.findAllStoreNames()).thenReturn(stores);

		assertSame(stores, merchantStoreService.findAllStoreNames());
	}

	@Test
	void findAllStoreNames_withCode_delegatesToRepository() throws Exception {
		List<MerchantStore> stores = Collections.singletonList(store(1, "N2"));
		when(merchantRepository.findAllStoreNames("GRP")).thenReturn(stores);

		assertSame(stores, merchantStoreService.findAllStoreNames("GRP"));
	}

	@Test
	void isStoreInGroup_whenEmpty_returnsFalse() throws Exception {
		MerchantStore store = store(4, "EMPTY");
		when(merchantRepository.findByCode("EMPTY")).thenReturn(store);
		when(merchantRepository.listByGroup("EMPTY", 4)).thenReturn(Collections.emptyList());

		assertFalse(merchantStoreService.isStoreInGroup("EMPTY"));
	}

	@Test
	void getParent_withParentId_resolvesParentFromRepository() throws Exception {
		MerchantStore parent = store(10, "PARENT");
		MerchantStore child = store(2, "CHILD");
		child.setParent(parent);
		child.setRetailer(false);
		when(merchantRepository.findByCode("CHILD")).thenReturn(child);
		when(merchantRepository.getById(parent.getId())).thenReturn(parent);

		assertSame(parent, merchantStoreService.getParent("CHILD"));
	}

	@Test
	void listAll_withoutName_delegatesWithNull() throws Exception {
		Page<MerchantStore> page = new PageImpl<>(Collections.emptyList());
		when(pageableMerchantRepository.listAll(eq(null), any(PageRequest.class))).thenReturn(page);

		merchantStoreService.listAll(Optional.empty(), 0, 5);

		verify(pageableMerchantRepository).listAll(eq(null), any(PageRequest.class));
	}

	@Test
	void listChildren_delegatesToPageableRepository() throws Exception {
		Page<MerchantStore> page = new PageImpl<>(Collections.singletonList(store(2, "CHILD")));
		when(pageableMerchantRepository.listByStore(eq("PARENT"), any(PageRequest.class))).thenReturn(page);

		Page<MerchantStore> result = merchantStoreService.listChildren("PARENT", 0, 10);

		assertEquals(1, result.getTotalElements());
	}

	@Test
	void listAll_withOptionalName_delegatesToRepository() throws Exception {
		Page<MerchantStore> page = new PageImpl<>(Collections.emptyList());
		when(pageableMerchantRepository.listAll(eq("Alpha"), any(PageRequest.class))).thenReturn(page);

		merchantStoreService.listAll(Optional.of("Alpha"), 0, 5);

		verify(pageableMerchantRepository).listAll(eq("Alpha"), any(PageRequest.class));
	}

	@Test
	void listByGroup_delegatesToPageableRepository() throws Exception {
		MerchantStore store = store(3, "GRP");
		when(merchantRepository.findByCode("GRP")).thenReturn(store);
		Page<MerchantStore> page = new PageImpl<>(Collections.singletonList(store));
		when(pageableMerchantRepository.listByGroup(eq("GRP"), eq(3), eq("name"), any(PageRequest.class)))
				.thenReturn(page);

		Page<MerchantStore> result = merchantStoreService.listByGroup(Optional.of("name"), "GRP", 0, 10);

		assertEquals(1, result.getTotalElements());
	}

	@Test
	void isStoreInGroup_whenMatches_returnsTrue() throws Exception {
		MerchantStore store = store(4, "GRP2");
		when(merchantRepository.findByCode("GRP2")).thenReturn(store);
		when(merchantRepository.listByGroup("GRP2", 4)).thenReturn(Collections.singletonList(store(5, "OTHER")));

		assertTrue(merchantStoreService.isStoreInGroup("GRP2"));
	}

	@Test
	void getByCriteria_delegatesToRepository() throws Exception {
		MerchantStoreCriteria criteria = new MerchantStoreCriteria();
		GenericEntityList<MerchantStore> list = new GenericEntityList<>();
		list.setList(Collections.singletonList(store(1, "X")));
		when(merchantRepository.listByCriteria(criteria)).thenReturn(list);

		assertEquals(1, merchantStoreService.getByCriteria(criteria).getList().size());
	}

	@Test
	void findAllStoreCodeNameEmail_delegatesToRepository() throws Exception {
		List<MerchantStore> stores = Collections.singletonList(store(1, "Z"));
		when(merchantRepository.findAllStoreCodeNameEmail()).thenReturn(stores);

		assertSame(stores, merchantStoreService.findAllStoreCodeNameEmail());
	}

	@Test
	void saveOrUpdate_delegatesToSave() throws Exception {
		MerchantStore store = store(null, "NEW");
		when(merchantRepository.saveAndFlush(store)).thenReturn(store);

		merchantStoreService.saveOrUpdate(store);

		verify(merchantRepository).saveAndFlush(store);
	}

	private static MerchantStore store(Integer id, String code) {
		MerchantStore store = new MerchantStore();
		store.setId(id);
		store.setCode(code);
		store.setStorename(code);
		store.setStorephone("555");
		store.setStorecity("City");
		store.setStorepostalcode("12345");
		store.setStoreEmailAddress(code.toLowerCase() + "@test.local");
		return store;
	}

	private void injectPageableRepository() {
		try {
			Field field = MerchantStoreServiceImpl.class.getDeclaredField("pageableMerchantRepository");
			field.setAccessible(true);
			field.set(merchantStoreService, pageableMerchantRepository);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}
}

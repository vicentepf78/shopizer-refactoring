package com.salesmanager.core.business.services.system;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.repositories.system.MerchantLogRepository;

@ExtendWith(MockitoExtension.class)
class MerchantLogServiceImplTest {

	@Mock
	private MerchantLogRepository merchantLogRepository;

	@Test
	void constructsWithRepository() {
		MerchantLogServiceImpl service = new MerchantLogServiceImpl(merchantLogRepository);
		assertNotNull(service);
	}
}

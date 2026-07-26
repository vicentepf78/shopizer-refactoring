package com.salesmanager.merchant.facade;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.merchant.support.MerchantConstants;
import com.salesmanager.merchant.support.TestDataFactory;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class MerchantConfigurationFacadeImplTest {

	@Autowired
	private MerchantConfigurationFacade merchantConfigurationFacade;
	@Autowired
	private TestDataFactory testDataFactory;

	private TestDataFactory.Seed seed;
	private Language language;

	@BeforeEach
	void setUp() {
		seed = testDataFactory.ensureDefaultAdmin();
		language = seed.language;
		testDataFactory.seedSocialConfig(seed.store, MerchantConstants.KEY_FACEBOOK_PAGE_URL, "https://fb.test");
	}

	@Test
	void getMerchantConfig_returnsFlagsAndSocial() {
		var config = merchantConfigurationFacade.getMerchantConfig(seed.store, language);
		assertThat(config.isAllowOnlinePurchase()).isTrue();
		assertThat(config.getFacebook()).isEqualTo("https://fb.test");
	}
}

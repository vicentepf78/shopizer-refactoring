package com.salesmanager.search.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import modules.commons.search.SearchModule;
import modules.commons.search.configuration.SearchConfiguration;

@ExtendWith(MockitoExtension.class)
class SearchModuleBootstrapTest {

	@Test
	void buildConfigurationLoadsMappingsAndSettings() throws Exception {
		ApplicationSearchConfiguration appConfig = new ApplicationSearchConfiguration();
		appConfig.setClusterName("test-cluster");
		appConfig.setSearchLanguages(java.util.Collections.singletonList("en"));

		SearchModuleBootstrap bootstrap = new SearchModuleBootstrap();
		ReflectionTestUtils.setField(bootstrap, "applicationSearchConfiguration", appConfig);

		SearchConfiguration config = bootstrap.buildConfiguration();
		assertThat(config.getClusterName()).isEqualTo("test-cluster");
		assertThat(config.getProductMappings()).containsKey("en");
		assertThat(config.getSettings()).containsKey("en");
	}

	@Test
	void initConfiguresSearchModuleWhenEnabled() throws Exception {
		SearchModule searchModule = mock(SearchModule.class);
		ApplicationSearchConfiguration appConfig = new ApplicationSearchConfiguration();
		appConfig.setClusterName("test-cluster");
		appConfig.setSearchLanguages(java.util.Collections.singletonList("en"));

		SearchModuleBootstrap bootstrap = new SearchModuleBootstrap();
		ReflectionTestUtils.setField(bootstrap, "searchModule", searchModule);
		ReflectionTestUtils.setField(bootstrap, "applicationSearchConfiguration", appConfig);
		ReflectionTestUtils.setField(bootstrap, "noIndex", false);

		bootstrap.init();
		verify(searchModule).configure(org.mockito.ArgumentMatchers.any(SearchConfiguration.class));
	}
}

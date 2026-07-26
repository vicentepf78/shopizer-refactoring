package com.salesmanager.test.shop.strangler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.contracts.client.MerchantServiceClient;
import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.shop.store.controller.content.facade.ContentFacade;
import com.salesmanager.shop.store.controller.store.facade.StoreFacade;
import com.salesmanager.shop.store.controller.system.MerchantConfigurationFacade;
import com.salesmanager.shop.store.controller.search.facade.SearchFacade;
import com.salesmanager.shop.strangler.content.ContentFacadeHttpAdapter;
import com.salesmanager.shop.strangler.content.StaticContentProxy;
import com.salesmanager.shop.strangler.merchant.MerchantConfigurationFacadeHttpAdapter;
import com.salesmanager.shop.strangler.merchant.MerchantServiceClientRestTemplateImpl;
import com.salesmanager.shop.strangler.merchant.MerchantStoreEntityHydrator;
import com.salesmanager.shop.strangler.merchant.StoreFacadeHttpAdapter;
import com.salesmanager.shop.strangler.config.Wave2Properties;
import com.salesmanager.shop.strangler.search.SearchBulkIndexOrchestrator;
import com.salesmanager.shop.strangler.search.SearchFacadeHttpAdapter;
import com.salesmanager.shop.tenant.TenantEntityBridge;
import com.salesmanager.shop.utils.ImageFilePath;

class Wave2StranglerConditionalBeanTest {

	@Test
	void wave2On_exactlyOneBeanPerFacadeInterface() {
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
			MockEnvironment env = new MockEnvironment();
			env.setProperty("wave2.strangler.enabled", "true");
			env.setProperty("wave2.content-service.base-url", "http://localhost:8083");
			env.setProperty("wave2.search-service.base-url", "http://localhost:8084");
			env.setProperty("wave2.merchant-service.base-url", "http://localhost:8085");
			ctx.setEnvironment(env);
			ctx.register(Wave2OnConfig.class);
			ctx.refresh();

			assertThat(ctx.getBean(ContentFacade.class)).isInstanceOf(ContentFacadeHttpAdapter.class);
			assertThat(ctx.getBean(SearchFacade.class)).isInstanceOf(SearchFacadeHttpAdapter.class);
			assertThat(ctx.getBean(StoreFacade.class)).isInstanceOf(StoreFacadeHttpAdapter.class);
			assertThat(ctx.getBean(MerchantConfigurationFacade.class))
					.isInstanceOf(MerchantConfigurationFacadeHttpAdapter.class);
			assertThat(ctx.getBeansOfType(ContentFacade.class)).hasSize(1);
			assertThat(ctx.getBeansOfType(SearchFacade.class)).hasSize(1);
			assertThat(ctx.getBeansOfType(StoreFacade.class)).hasSize(1);
			assertThat(ctx.getBeansOfType(MerchantConfigurationFacade.class)).hasSize(1);
		}
	}

	@Test
	void wave2Off_exactlyOneInProcessBeanPerFacadeInterface() {
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
			MockEnvironment env = new MockEnvironment();
			env.setProperty("wave2.strangler.enabled", "false");
			ctx.setEnvironment(env);
			ctx.register(MonolithConfig.class);
			ctx.refresh();

			assertThat(ctx.getBean(ContentFacade.class)).isInstanceOf(InProcessContentFacade.class);
			assertThat(ctx.getBean(SearchFacade.class)).isInstanceOf(InProcessSearchFacade.class);
			assertThat(ctx.getBean(StoreFacade.class)).isInstanceOf(InProcessStoreFacade.class);
			assertThat(ctx.getBean(MerchantConfigurationFacade.class))
					.isInstanceOf(InProcessMerchantConfigurationFacade.class);
		}
	}

	@Configuration
	static class Wave2OnConfig {
		@Bean
		RestTemplate wave2RestTemplate() {
			return new RestTemplate();
		}

		@Bean
		Wave2Properties wave2Properties() {
			Wave2Properties properties = new Wave2Properties();
			properties.getContentService().setBaseUrl("http://localhost:8083");
			properties.getSearchService().setBaseUrl("http://localhost:8084");
			properties.getMerchantService().setBaseUrl("http://localhost:8085");
			return properties;
		}

		@Bean
		SearchBulkIndexOrchestrator searchBulkIndexOrchestrator() {
			return org.mockito.Mockito.mock(SearchBulkIndexOrchestrator.class);
		}

		@Bean
		StaticContentProxy staticContentProxy(RestTemplate wave2RestTemplate) {
			return new StaticContentProxy(wave2RestTemplate, "http://localhost:8083");
		}

		@Bean
		ImageFilePath imageFilePath() {
			return org.mockito.Mockito.mock(ImageFilePath.class);
		}

		@Bean
		MerchantStoreEntityHydrator merchantStoreEntityHydrator() {
			return new MerchantStoreEntityHydrator();
		}

		@Bean
		MerchantServiceClient merchantServiceClient(RestTemplate wave2RestTemplate, Wave2Properties properties) {
			return new MerchantServiceClientRestTemplateImpl(wave2RestTemplate, properties);
		}

		@Bean
		ContentFacadeHttpAdapter contentFacadeHttpAdapter(
				RestTemplate wave2RestTemplate,
				StaticContentProxy staticContentProxy,
				ImageFilePath imageFilePath) {
			return new ContentFacadeHttpAdapter(
					wave2RestTemplate, "http://localhost:8083", imageFilePath, staticContentProxy);
		}

		@Bean
		TenantEntityBridge tenantEntityBridge() {
			return org.mockito.Mockito.mock(TenantEntityBridge.class);
		}

		@Bean
		SearchFacadeHttpAdapter searchFacadeHttpAdapter(
				RestTemplate wave2RestTemplate,
				SearchBulkIndexOrchestrator orchestrator,
				TenantEntityBridge tenantEntityBridge) {
			return new SearchFacadeHttpAdapter(wave2RestTemplate, "http://localhost:8084", orchestrator,
					tenantEntityBridge);
		}

		@Bean
		StoreFacadeHttpAdapter storeFacadeHttpAdapter(
				RestTemplate wave2RestTemplate,
				MerchantServiceClient merchantServiceClient,
				MerchantStoreEntityHydrator hydrator) {
			return new StoreFacadeHttpAdapter(
					wave2RestTemplate, "http://localhost:8085", merchantServiceClient, hydrator);
		}

		@Bean
		MerchantConfigurationFacadeHttpAdapter merchantConfigurationFacadeHttpAdapter(
				RestTemplate wave2RestTemplate) {
			return new MerchantConfigurationFacadeHttpAdapter(wave2RestTemplate, "http://localhost:8085");
		}
	}

	@Configuration
	static class MonolithConfig {
		@Bean
		@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "false", matchIfMissing = true)
		InProcessContentFacade inProcessContentFacade() {
			return new InProcessContentFacade();
		}

		@Bean
		@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "false", matchIfMissing = true)
		InProcessSearchFacade inProcessSearchFacade() {
			return new InProcessSearchFacade();
		}

		@Bean
		@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "false", matchIfMissing = true)
		InProcessStoreFacade inProcessStoreFacade() {
			return new InProcessStoreFacade();
		}

		@Bean
		@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "false", matchIfMissing = true)
		InProcessMerchantConfigurationFacade inProcessMerchantConfigurationFacade() {
			return new InProcessMerchantConfigurationFacade();
		}

		@Bean
		@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
		ContentFacadeHttpAdapter contentFacadeHttpAdapter() {
			return new ContentFacadeHttpAdapter(
					new RestTemplate(),
					"http://localhost:8083",
					org.mockito.Mockito.mock(ImageFilePath.class),
					new StaticContentProxy(new RestTemplate(), "http://localhost:8083"));
		}
	}

	static class InProcessContentFacade implements ContentFacade {
		@Override
		public com.salesmanager.shop.model.content.ContentFolder getContentFolder(String folder,
				com.salesmanager.core.model.merchant.MerchantStore store) {
			return new com.salesmanager.shop.model.content.ContentFolder();
		}

		@Override
		public String absolutePath(com.salesmanager.core.model.merchant.MerchantStore store, String file) {
			return file;
		}

		@Override
		public void delete(com.salesmanager.core.model.merchant.MerchantStore store, String fileName, String fileType) {
		}

		@Override
		public void delete(com.salesmanager.core.model.merchant.MerchantStore store, Long id) {
		}

		@Override
		public com.salesmanager.shop.model.entity.ReadableEntityList<com.salesmanager.shop.model.content.page.ReadableContentPage> getContentPages(
				com.salesmanager.core.model.merchant.MerchantStore store,
				com.salesmanager.core.model.reference.language.Language language, int page, int count) {
			return new com.salesmanager.shop.model.entity.ReadableEntityList<>();
		}

		@Override
		public com.salesmanager.shop.model.content.page.ReadableContentPage getContentPage(String code,
				com.salesmanager.core.model.merchant.MerchantStore store,
				com.salesmanager.core.model.reference.language.Language language) {
			return new com.salesmanager.shop.model.content.page.ReadableContentPage();
		}

		@Override
		public com.salesmanager.shop.model.content.page.ReadableContentPage getContentPageByName(String name,
				com.salesmanager.core.model.merchant.MerchantStore store,
				com.salesmanager.core.model.reference.language.Language language) {
			return new com.salesmanager.shop.model.content.page.ReadableContentPage();
		}

		@Override
		public com.salesmanager.shop.model.content.box.ReadableContentBox getContentBox(String code,
				com.salesmanager.core.model.merchant.MerchantStore store,
				com.salesmanager.core.model.reference.language.Language language) {
			return new com.salesmanager.shop.model.content.box.ReadableContentBox();
		}

		@Override
		public boolean codeExist(String code, String type, com.salesmanager.core.model.merchant.MerchantStore store) {
			return false;
		}

		@Override
		public com.salesmanager.shop.model.entity.ReadableEntityList<com.salesmanager.shop.model.content.box.ReadableContentBox> getContentBoxes(
				com.salesmanager.core.model.content.ContentType type, String codePrefix,
				com.salesmanager.core.model.merchant.MerchantStore store,
				com.salesmanager.core.model.reference.language.Language language, int start, int count) {
			return new com.salesmanager.shop.model.entity.ReadableEntityList<>();
		}

		@Override
		public com.salesmanager.shop.model.entity.ReadableEntityList<com.salesmanager.shop.model.content.box.ReadableContentBox> getContentBoxes(
				com.salesmanager.core.model.content.ContentType type,
				com.salesmanager.core.model.merchant.MerchantStore store,
				com.salesmanager.core.model.reference.language.Language language, int start, int count) {
			return new com.salesmanager.shop.model.entity.ReadableEntityList<>();
		}

		@Override
		public void addContentFile(com.salesmanager.shop.model.content.ContentFile file, String merchantStoreCode) {
		}

		@Override
		public void addContentFiles(java.util.List<com.salesmanager.shop.model.content.ContentFile> file,
				String merchantStoreCode) {
		}

		@Override
		public Long saveContentPage(com.salesmanager.shop.model.content.page.PersistableContentPage page,
				com.salesmanager.core.model.merchant.MerchantStore merchantStore,
				com.salesmanager.core.model.reference.language.Language language) {
			return 1L;
		}

		@Override
		public void updateContentPage(Long id, com.salesmanager.shop.model.content.page.PersistableContentPage page,
				com.salesmanager.core.model.merchant.MerchantStore merchantStore,
				com.salesmanager.core.model.reference.language.Language language) {
		}

		@Override
		public void deleteContent(Long id, com.salesmanager.core.model.merchant.MerchantStore merchantStore) {
		}

		@Override
		public Long saveContentBox(com.salesmanager.shop.model.content.box.PersistableContentBox box,
				com.salesmanager.core.model.merchant.MerchantStore merchantStore,
				com.salesmanager.core.model.reference.language.Language language) {
			return 1L;
		}

		@Override
		public void updateContentBox(Long id, com.salesmanager.shop.model.content.box.PersistableContentBox box,
				com.salesmanager.core.model.merchant.MerchantStore merchantStore,
				com.salesmanager.core.model.reference.language.Language language) {
		}

		@Override
		public com.salesmanager.shop.model.content.ReadableContentFull getContent(String code,
				com.salesmanager.core.model.merchant.MerchantStore store,
				com.salesmanager.core.model.reference.language.Language language) {
			return new com.salesmanager.shop.model.content.ReadableContentFull();
		}

		@Override
		public java.util.List<com.salesmanager.shop.model.content.ReadableContentEntity> getContents(
				java.util.Optional<String> type, com.salesmanager.core.model.merchant.MerchantStore store,
				com.salesmanager.core.model.reference.language.Language language) {
			return java.util.Collections.emptyList();
		}

		@Override
		public void renameFile(com.salesmanager.core.model.merchant.MerchantStore store,
				com.salesmanager.core.model.content.FileContentType fileType, String originalName, String newName) {
		}

		@Override
		public com.salesmanager.core.model.content.OutputContentFile download(
				com.salesmanager.core.model.merchant.MerchantStore store,
				com.salesmanager.core.model.content.FileContentType fileType, String fileName) {
			return null;
		}
	}

	static class InProcessSearchFacade implements SearchFacade {
		@Override
		public void indexAllData(MerchantStoreId storeId) {
		}

		@Override
		public java.util.List<modules.commons.search.request.SearchItem> search(
				MerchantStoreId storeId,
				LanguageCode language,
				com.salesmanager.shop.model.catalog.SearchProductRequest searchRequest) {
			return java.util.Collections.emptyList();
		}

		@Override
		public com.salesmanager.shop.model.entity.ValueList autocompleteRequest(String query,
				MerchantStoreId storeId,
				LanguageCode language) {
			return new com.salesmanager.shop.model.entity.ValueList();
		}
	}

	static class InProcessStoreFacade implements StoreFacade {
		@Override
		public com.salesmanager.core.model.merchant.MerchantStore getByCode(javax.servlet.http.HttpServletRequest request) {
			return new com.salesmanager.core.model.merchant.MerchantStore();
		}

		@Override
		public com.salesmanager.core.model.merchant.MerchantStore get(String code) {
			return new com.salesmanager.core.model.merchant.MerchantStore();
		}

		@Override
		public com.salesmanager.core.model.merchant.MerchantStore getByCode(String code) {
			return get(code);
		}

		@Override
		public java.util.List<com.salesmanager.core.model.reference.language.Language> supportedLanguages(
				com.salesmanager.core.model.merchant.MerchantStore store) {
			return java.util.Collections.emptyList();
		}

		@Override
		public com.salesmanager.shop.model.store.ReadableMerchantStore getByCode(String code, String lang) {
			return new com.salesmanager.shop.model.store.ReadableMerchantStore();
		}

		@Override
		public com.salesmanager.shop.model.store.ReadableMerchantStore getFullByCode(String code, String lang) {
			return new com.salesmanager.shop.model.store.ReadableMerchantStore();
		}

		@Override
		public com.salesmanager.shop.model.store.ReadableMerchantStoreList findAll(
				com.salesmanager.core.model.merchant.MerchantStoreCriteria criteria,
				com.salesmanager.core.model.reference.language.Language language, int page, int count) {
			return new com.salesmanager.shop.model.store.ReadableMerchantStoreList();
		}

		@Override
		public com.salesmanager.shop.model.store.ReadableMerchantStoreList getChildStores(
				com.salesmanager.core.model.reference.language.Language language, String code, int start, int count) {
			return new com.salesmanager.shop.model.store.ReadableMerchantStoreList();
		}

		@Override
		public com.salesmanager.shop.model.store.ReadableMerchantStore getByCode(String code,
				com.salesmanager.core.model.reference.language.Language lang) {
			return new com.salesmanager.shop.model.store.ReadableMerchantStore();
		}

		@Override
		public com.salesmanager.shop.model.store.ReadableMerchantStore getFullByCode(String code,
				com.salesmanager.core.model.reference.language.Language language) {
			return new com.salesmanager.shop.model.store.ReadableMerchantStore();
		}

		@Override
		public boolean existByCode(String code) {
			return false;
		}

		@Override
		public com.salesmanager.shop.model.store.ReadableMerchantStoreList getByCriteria(
				com.salesmanager.core.model.merchant.MerchantStoreCriteria criteria,
				com.salesmanager.core.model.reference.language.Language lang) {
			return new com.salesmanager.shop.model.store.ReadableMerchantStoreList();
		}

		@Override
		public void create(com.salesmanager.shop.model.store.PersistableMerchantStore store) {
		}

		@Override
		public void update(com.salesmanager.shop.model.store.PersistableMerchantStore store) {
		}

		@Override
		public void delete(String code) {
		}

		@Override
		public com.salesmanager.shop.model.store.ReadableBrand getBrand(String code) {
			return new com.salesmanager.shop.model.store.ReadableBrand();
		}

		@Override
		public void createBrand(String merchantStoreCode, com.salesmanager.shop.model.store.PersistableBrand brand) {
		}

		@Override
		public void deleteLogo(String code) {
		}

		@Override
		public void addStoreLogo(String code, com.salesmanager.core.model.content.InputContentFile cmsContentImage) {
		}

		@Override
		public java.util.List<com.salesmanager.shop.model.store.ReadableMerchantStore> getMerchantStoreNames(
				com.salesmanager.core.model.merchant.MerchantStoreCriteria criteria) {
			return java.util.Collections.emptyList();
		}
	}

	static class InProcessMerchantConfigurationFacade implements MerchantConfigurationFacade {
		@Override
		public com.salesmanager.shop.model.system.Configs getMerchantConfig(
				com.salesmanager.core.model.merchant.MerchantStore merchantStore,
				com.salesmanager.core.model.reference.language.Language language) {
			return new com.salesmanager.shop.model.system.Configs();
		}
	}
}

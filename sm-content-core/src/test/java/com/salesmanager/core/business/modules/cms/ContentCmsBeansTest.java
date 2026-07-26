package com.salesmanager.core.business.modules.cms;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.salesmanager.core.business.modules.cms.content.StaticContentFileManager;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ContentCmsBeansTest.CmsConfig.class)
class ContentCmsBeansTest {

	@Configuration
	@ImportResource("classpath:spring/shopizer-content-cms.xml")
	@PropertySource("classpath:application.properties")
	static class CmsConfig {

		@Bean
		static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
			return new PropertySourcesPlaceholderConfigurer();
		}
	}

	@Autowired
	@Qualifier("contentFileManager")
	private StaticContentFileManager contentFileManager;

	@Autowired
	private org.springframework.context.ApplicationContext applicationContext;

	@Test
	void contentFileManagerResolvesViaImportResource() {
		assertNotNull(contentFileManager);
	}

	@Test
	void productFileManagerBeanIsNotRegistered() {
		assertThrows(NoSuchBeanDefinitionException.class,
				() -> applicationContext.getBean("productFileManager"));
	}
}

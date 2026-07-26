package com.salesmanager.search.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import modules.commons.search.SearchModule;
import modules.commons.search.configuration.SearchConfiguration;

@Component
@EnableConfigurationProperties(ApplicationSearchConfiguration.class)
public class SearchModuleBootstrap {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchModuleBootstrap.class);

	private static final String SETTINGS = "search/SETTINGS";
	private static final String PRODUCT_MAPPING_DEFAULT = "search/MAPPINGS.json";
	private static final String KEYWORDS_MAPPING_DEFAULT = "{\"properties\":{\"id\":{\"type\":\"long\"}}}";

	@Value("${search.noindex:false}")
	private boolean noIndex;

	@Autowired(required = false)
	private SearchModule searchModule;

	@Autowired
	private ApplicationSearchConfiguration applicationSearchConfiguration;

	@PostConstruct
	public void init() {
		if (searchModule == null || noIndex) {
			return;
		}
		try {
			searchModule.configure(buildConfiguration());
		} catch (Exception e) {
			LOGGER.error("SearchModule cannot be configured [{}]", e.getMessage(), e);
		}
	}

	SearchConfiguration buildConfiguration() throws Exception {
		SearchConfiguration config = new SearchConfiguration();
		config.setClusterName(applicationSearchConfiguration.getClusterName());
		config.setHosts(applicationSearchConfiguration.getHost());
		config.setCredentials(applicationSearchConfiguration.getCredentials());

		config.setLanguages(applicationSearchConfiguration.getSearchLanguages());
		for (String language : config.getLanguages()) {
			mappings(config, language);
			settings(config, language);
		}
		return config;
	}

	private void settings(SearchConfiguration config, String language) throws IOException {
		Validate.notEmpty(language, "Configuration requires language");
		String settings = loadClassPathResource(SETTINGS + "_DEFAULT.json");
		if ("en".equals(language)) {
			settings = loadClassPathResource(SETTINGS + "_" + language + ".json");
		}
		config.getSettings().put(language, settings);
	}

	private void mappings(SearchConfiguration config, String language) throws IOException {
		Validate.notEmpty(language, "Configuration requires language");
		config.getProductMappings().put(language, loadClassPathResource(PRODUCT_MAPPING_DEFAULT));
		config.getKeywordsMappings().put(language, KEYWORDS_MAPPING_DEFAULT);
	}

	private String loadClassPathResource(String file) throws IOException {
		ClassPathResource resource = new ClassPathResource(file);
		try (java.io.InputStream in = resource.getInputStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}

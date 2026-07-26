package com.salesmanager.merchant.facade;

import static com.salesmanager.merchant.support.MerchantConstants.KEY_FACEBOOK_PAGE_URL;
import static com.salesmanager.merchant.support.MerchantConstants.KEY_GOOGLE_ANALYTICS_URL;
import static com.salesmanager.merchant.support.MerchantConstants.KEY_INSTAGRAM_URL;
import static com.salesmanager.merchant.support.MerchantConstants.KEY_PINTEREST_PAGE_URL;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.salesmanager.contracts.merchant.Configs;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.system.MerchantConfigurationService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.system.MerchantConfig;
import com.salesmanager.core.model.system.MerchantConfiguration;
import com.salesmanager.merchant.support.ServiceRuntimeException;

@Service
public class MerchantConfigurationFacadeImpl implements MerchantConfigurationFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(MerchantConfigurationFacadeImpl.class);

	private final MerchantConfigurationService merchantConfigurationService;

	@Value("${config.displayShipping:}")
	private String displayShipping;

	public MerchantConfigurationFacadeImpl(MerchantConfigurationService merchantConfigurationService) {
		this.merchantConfigurationService = merchantConfigurationService;
	}

	@Override
	public Configs getMerchantConfig(MerchantStore merchantStore, Language language) {
		MerchantConfig configs = getMerchantConfig(merchantStore);

		Configs readableConfig = new Configs();
		readableConfig.setAllowOnlinePurchase(configs.isAllowPurchaseItems());
		readableConfig.setDisplaySearchBox(configs.isDisplaySearchBox());
		readableConfig.setDisplayContactUs(configs.isDisplayContactUs());
		readableConfig.setDisplayCustomerSection(configs.isDisplayCustomerSection());
		readableConfig.setDisplayAddToCartOnFeaturedItems(configs.isDisplayAddToCartOnFeaturedItems());
		readableConfig.setDisplayCustomerAgreement(configs.isDisplayCustomerAgreement());
		readableConfig.setDisplayPagesMenu(configs.isDisplayPagesMenu());

		getConfigValue(KEY_FACEBOOK_PAGE_URL, merchantStore).ifPresent(readableConfig::setFacebook);
		getConfigValue(KEY_GOOGLE_ANALYTICS_URL, merchantStore).ifPresent(readableConfig::setGa);
		getConfigValue(KEY_INSTAGRAM_URL, merchantStore).ifPresent(readableConfig::setInstagram);
		getConfigValue(KEY_PINTEREST_PAGE_URL, merchantStore).ifPresent(readableConfig::setPinterest);

		readableConfig.setDisplayShipping(false);
		try {
			if (!StringUtils.isBlank(displayShipping)) {
				readableConfig.setDisplayShipping(Boolean.valueOf(displayShipping));
			}
		} catch (Exception e) {
			LOGGER.error("Cannot parse value of {}", displayShipping);
		}

		return readableConfig;
	}

	private MerchantConfig getMerchantConfig(MerchantStore merchantStore) {
		try {
			MerchantConfig config = merchantConfigurationService.getMerchantConfig(merchantStore);
			return config != null ? config : new MerchantConfig();
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e.getMessage(), e);
		}
	}

	private Optional<String> getConfigValue(String key, MerchantStore merchantStore) {
		return getMerchantConfiguration(key, merchantStore).map(MerchantConfiguration::getValue);
	}

	private Optional<MerchantConfiguration> getMerchantConfiguration(String key, MerchantStore merchantStore) {
		try {
			return Optional.ofNullable(merchantConfigurationService.getMerchantConfiguration(key, merchantStore));
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e.getMessage(), e);
		}
	}
}

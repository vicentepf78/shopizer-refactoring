package com.salesmanager.merchant.populator;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import com.salesmanager.contracts.merchant.PersistableMerchantStore;
import com.salesmanager.contracts.reference.PersistableAddress;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.core.constants.MeasureUnit;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.merchant.reference.CurrencyRepository;
import com.salesmanager.merchant.reference.ReferenceEntityMapper;
import com.salesmanager.merchant.support.ConversionRuntimeException;
import com.salesmanager.merchant.support.DateUtil;

@Component
public class PersistableMerchantStorePopulator {

	private final ReferenceEntityMapper referenceEntityMapper;
	private final CurrencyRepository currencyRepository;
	private final MerchantStoreService merchantStoreService;

	public PersistableMerchantStorePopulator(
			ReferenceEntityMapper referenceEntityMapper,
			CurrencyRepository currencyRepository,
			MerchantStoreService merchantStoreService) {
		this.referenceEntityMapper = referenceEntityMapper;
		this.currencyRepository = currencyRepository;
		this.merchantStoreService = merchantStoreService;
	}

	public MerchantStore populate(
			PersistableMerchantStore source,
			MerchantStore target,
			MerchantStore store,
			Language language) throws ConversionException {
		Validate.notNull(source, "PersistableMerchantStore must not be null");

		if (target == null) {
			target = new MerchantStore();
		}

		target.setCode(source.getCode());
		if (source.getId() != 0) {
			target.setId(source.getId());
		}

		if (store != null && store.getStoreLogo() != null) {
			target.setStoreLogo(store.getStoreLogo());
		}

		if (!StringUtils.isEmpty(source.getInBusinessSince())) {
			try {
				Date dt = DateUtil.getDate(source.getInBusinessSince());
				target.setInBusinessSince(dt);
			} catch (Exception e) {
				throw new ConversionException("Cannot parse date [" + source.getInBusinessSince() + "]", e);
			}
		}

		if (source.getDimension() != null) {
			target.setSeizeunitcode(source.getDimension().name());
		}
		if (source.getWeight() != null) {
			target.setWeightunitcode(source.getWeight().name());
		}
		target.setCurrencyFormatNational(source.isCurrencyFormatNational());
		target.setStorename(source.getName());
		target.setStorephone(source.getPhone());
		target.setStoreEmailAddress(source.getEmail());
		target.setUseCache(source.isUseCache());
		target.setRetailer(source.isRetailer());

		if (!StringUtils.isBlank(source.getRetailerStore())) {
			if (source.getRetailerStore().equals(source.getCode())) {
				throw new ConversionException("Parent store [" + source.getRetailerStore() + "] cannot be parent of current store");
			}
			try {
				MerchantStore parent = merchantStoreService.getByCode(source.getRetailerStore());
				if (parent == null) {
					throw new ConversionException("Parent store [" + source.getRetailerStore() + "] does not exist");
				}
				target.setParent(parent);
			} catch (ServiceException e) {
				throw new ConversionException(e);
			}
		}

		try {
			if (!StringUtils.isEmpty(source.getDefaultLanguage())) {
				target.setDefaultLanguage(referenceEntityMapper.toLanguage(source.getDefaultLanguage()));
			}

			if (!StringUtils.isEmpty(source.getCurrency())) {
				Currency currency = currencyRepository.getByCode(source.getCurrency());
				if (currency == null) {
					throw new ConversionException("Currency [" + source.getCurrency() + "] not found");
				}
				target.setCurrency(currency);
			} else {
				Currency currency = currencyRepository.getByCode(Constants.DEFAULT_CURRENCY.getCurrencyCode());
				if (currency != null) {
					target.setCurrency(currency);
				}
			}

			List<String> languages = source.getSupportedLanguages();
			if (!CollectionUtils.isEmpty(languages)) {
				for (String lang : languages) {
					target.getLanguages().add(referenceEntityMapper.toLanguage(lang));
				}
			}
		} catch (ConversionRuntimeException e) {
			throw new ConversionException(e.getMessage(), e);
		}

		PersistableAddress address = source.getAddress();
		if (address != null) {
			try {
				String langCode = language != null ? language.getCode() : null;
				target.setCountry(referenceEntityMapper.toCountry(address.getCountry(), langCode));
				target.setStorestateprovince(address.getStateProvince());
				target.setStoreaddress(address.getAddress());
				target.setStorecity(address.getCity());
				target.setStorepostalcode(address.getPostalCode());
			} catch (ConversionRuntimeException e) {
				throw new ConversionException(e.getMessage(), e);
			}
		}

		if (StringUtils.isNotEmpty(source.getTemplate())) {
			target.setStoreTemplate(source.getTemplate());
		}

		return target;
	}
}

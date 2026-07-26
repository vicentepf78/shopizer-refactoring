package com.salesmanager.merchant.facade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.salesmanager.contracts.client.ContentServiceClient;
import com.salesmanager.contracts.content.ReadableImage;
import com.salesmanager.contracts.merchant.MerchantConfigEntity;
import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.contracts.merchant.PersistableBrand;
import com.salesmanager.contracts.merchant.PersistableMerchantStore;
import com.salesmanager.contracts.merchant.ReadableBrand;
import com.salesmanager.contracts.merchant.ReadableMerchantStore;
import com.salesmanager.contracts.merchant.ReadableMerchantStoreList;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.services.system.MerchantConfigurationService;
import com.salesmanager.core.constants.MeasureUnit;
import com.salesmanager.core.model.common.GenericEntityList;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.merchant.MerchantStoreCriteria;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.system.MerchantConfiguration;
import com.salesmanager.core.model.system.MerchantConfigurationType;
import com.salesmanager.merchant.populator.MerchantStoreSnapshotPopulator;
import com.salesmanager.merchant.populator.PersistableMerchantStorePopulator;
import com.salesmanager.merchant.populator.ReadableMerchantStorePopulator;
import com.salesmanager.merchant.support.ConversionRuntimeException;
import com.salesmanager.merchant.support.ResourceNotFoundException;
import com.salesmanager.merchant.support.ServiceRuntimeException;
import com.salesmanager.merchant.util.MerchantLogoPath;

@Service
public class StoreFacadeImpl implements StoreFacade {

	private static final Logger LOG = LoggerFactory.getLogger(StoreFacadeImpl.class);

	private final MerchantStoreService merchantStoreService;
	private final MerchantConfigurationService merchantConfigurationService;
	private final LanguageService languageService;
	private final ContentServiceClient contentServiceClient;
	private final PersistableMerchantStorePopulator persistableMerchantStorePopulator;
	private final ReadableMerchantStorePopulator readableMerchantStorePopulator;
	private final MerchantStoreSnapshotPopulator merchantStoreSnapshotPopulator;
	private final MerchantLogoPath merchantLogoPath;

	public StoreFacadeImpl(
			MerchantStoreService merchantStoreService,
			MerchantConfigurationService merchantConfigurationService,
			LanguageService languageService,
			ContentServiceClient contentServiceClient,
			PersistableMerchantStorePopulator persistableMerchantStorePopulator,
			ReadableMerchantStorePopulator readableMerchantStorePopulator,
			MerchantStoreSnapshotPopulator merchantStoreSnapshotPopulator,
			@Qualifier("img") MerchantLogoPath merchantLogoPath) {
		this.merchantStoreService = merchantStoreService;
		this.merchantConfigurationService = merchantConfigurationService;
		this.languageService = languageService;
		this.contentServiceClient = contentServiceClient;
		this.persistableMerchantStorePopulator = persistableMerchantStorePopulator;
		this.readableMerchantStorePopulator = readableMerchantStorePopulator;
		this.merchantStoreSnapshotPopulator = merchantStoreSnapshotPopulator;
		this.merchantLogoPath = merchantLogoPath;
	}

	@Override
	public MerchantStore get(String code) {
		try {
			return merchantStoreService.getByCode(code);
		} catch (ServiceException e) {
			LOG.error("Error while getting MerchantStore", e);
			throw new ServiceRuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public MerchantStore getByCode(String code) {
		return getMerchantStoreByCode(code);
	}

	@Override
	public ReadableMerchantStore getByCode(String code, String lang) {
		return getByCode(code, resolveLanguage(lang));
	}

	@Override
	public ReadableMerchantStore getFullByCode(String code, String lang) {
		return getFullByCode(code, resolveLanguage(lang));
	}

	@Override
	public ReadableMerchantStore getByCode(String code, Language language) {
		return convertMerchantStoreToReadableMerchantStore(language, getMerchantStoreByCode(code));
	}

	@Override
	public ReadableMerchantStore getFullByCode(String code, Language language) {
		return convertMerchantStoreToReadableMerchantStore(language, getMerchantStoreByCode(code));
	}

	@Override
	public boolean existByCode(String code) {
		try {
			return merchantStoreService.getByCode(code) != null;
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void create(PersistableMerchantStore store) {
		Validate.notNull(store, "PersistableMerchantStore must not be null");
		Validate.notNull(store.getCode(), "PersistableMerchantStore.code must not be null");

		if (get(store.getCode()) != null) {
			throw new ServiceRuntimeException("MerchantStore " + store.getCode() + " already exists");
		}

		MerchantStore mStore = convertPersistableMerchantStoreToMerchantStore(store, languageService.defaultLanguage());
		createMerchantStore(mStore);
	}

	@Override
	public void update(PersistableMerchantStore store) {
		Validate.notNull(store);
		MerchantStore mStore = mergePersistableMerchantStoreToMerchantStore(
				store, store.getCode(), languageService.defaultLanguage());
		updateMerchantStore(mStore);
	}

	@Override
	public ReadableMerchantStoreList getByCriteria(MerchantStoreCriteria criteria, Language lang) {
		return getMerchantStoresByCriteria(criteria, lang);
	}

	@Override
	public void delete(String code) {
		if (MerchantStore.DEFAULT_STORE.equals(code.toUpperCase())) {
			throw new ServiceRuntimeException("Cannot remove default store");
		}
		MerchantStore mStore = getMerchantStoreByCode(code);
		try {
			merchantStoreService.delete(mStore);
		} catch (Exception e) {
			LOG.error("Error while deleting MerchantStore", e);
			throw new ServiceRuntimeException("Error while deleting MerchantStore " + e.getMessage());
		}
	}

	@Override
	public ReadableBrand getBrand(String code) {
		MerchantStore mStore = getMerchantStoreByCode(code);
		ReadableBrand readableBrand = new ReadableBrand();
		if (!StringUtils.isEmpty(mStore.getStoreLogo())) {
			ReadableImage image = new ReadableImage();
			image.setName(mStore.getStoreLogo());
			image.setPath(merchantLogoPath.buildStoreLogoFilePath(mStore));
			readableBrand.setLogo(image);
		}
		readableBrand.getSocialNetworks().addAll(getMerchantConfigEntities(mStore));
		return readableBrand;
	}

	@Override
	public void deleteLogo(String code) {
		MerchantStore store = getByCode(code);
		String image = store.getStoreLogo();
		store.setStoreLogo(null);
		updateMerchantStore(store);
		if (!StringUtils.isEmpty(image)) {
			try {
				contentServiceClient.deleteLogo(store.getCode(), image);
			} catch (RuntimeException e) {
				// ponytail: orphan blob tolerated per AD-014; ops cleanup if content stays down
				LOG.warn("Orphan blob after logo DB clear store={} file={}", code, image, e);
			}
		}
	}

	@Override
	public void addStoreLogo(String code, String fileName, byte[] content, String contentType) {
		contentServiceClient.uploadLogo(code, fileName, content, contentType);
		try {
			MerchantStore store = getByCode(code);
			store.setStoreLogo(fileName);
			saveMerchantStore(store);
		} catch (RuntimeException e) {
			try {
				contentServiceClient.deleteLogo(code, fileName);
			} catch (RuntimeException compensateEx) {
				LOG.error("Logo compensate delete failed for store {} file {}", code, fileName, compensateEx);
			}
			throw e instanceof ServiceRuntimeException
					? (ServiceRuntimeException) e
					: new ServiceRuntimeException("Failed to persist store logo", e);
		}
	}

	@Override
	public void createBrand(String merchantStoreCode, PersistableBrand brand) {
		MerchantStore mStore = getMerchantStoreByCode(merchantStoreCode);
		List<MerchantConfigEntity> createdConfigs = brand.getSocialNetworks();
		List<MerchantConfiguration> configurations = createdConfigs.stream()
				.map(config -> convertToMerchantConfiguration(config, MerchantConfigurationType.SOCIAL))
				.collect(Collectors.toList());
		try {
			for (MerchantConfiguration mConfigs : configurations) {
				mConfigs.setMerchantStore(mStore);
				if (!StringUtils.isEmpty(mConfigs.getValue())) {
					mConfigs.setMerchantConfigurationType(MerchantConfigurationType.SOCIAL);
					merchantConfigurationService.saveOrUpdate(mConfigs);
				} else {
					MerchantConfiguration config = merchantConfigurationService
							.getMerchantConfiguration(mConfigs.getKey(), mStore);
					if (config != null) {
						merchantConfigurationService.delete(config);
					}
				}
			}
		} catch (ServiceException se) {
			throw new ServiceRuntimeException(se.getMessage(), se);
		}
	}

	@Override
	public ReadableMerchantStoreList getChildStores(Language language, String code, int page, int count) {
		try {
			MerchantStore retailer = getByCode(code);
			if (retailer == null) {
				throw new ResourceNotFoundException("Merchant [" + code + "] not found");
			}
			if (retailer.isRetailer() == null || !retailer.isRetailer().booleanValue()) {
				throw new ResourceNotFoundException("Merchant [" + code + "] not a retailer");
			}

			Page<MerchantStore> children = merchantStoreService.listChildren(code, page, count);
			List<ReadableMerchantStore> readableStores = new ArrayList<>();
			ReadableMerchantStoreList readableList = new ReadableMerchantStoreList();
			if (!CollectionUtils.isEmpty(children.getContent())) {
				for (MerchantStore store : children) {
					readableStores.add(convertMerchantStoreToReadableMerchantStore(language, store));
				}
			}
			readableList.setData(readableStores);
			readableList.setRecordsFiltered(children.getSize());
			readableList.setTotalPages(children.getTotalPages());
			readableList.setRecordsTotal(children.getTotalElements());
			readableList.setNumber(children.getNumber());
			return readableList;
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public ReadableMerchantStoreList findAll(MerchantStoreCriteria criteria, Language language, int page, int count) {
		try {
			Page<MerchantStore> stores;
			List<ReadableMerchantStore> readableStores = new ArrayList<>();
			ReadableMerchantStoreList readableList = new ReadableMerchantStoreList();

			Optional<String> code = Optional.ofNullable(criteria.getStoreCode());
			Optional<String> name = Optional.ofNullable(criteria.getName());
			if (code.isPresent()) {
				stores = merchantStoreService.listByGroup(name, code.get(), page, count);
			} else if (criteria.isRetailers()) {
				stores = merchantStoreService.listAllRetailers(name, page, count);
			} else {
				stores = merchantStoreService.listAll(name, page, count);
			}

			if (!CollectionUtils.isEmpty(stores.getContent())) {
				for (MerchantStore store : stores) {
					readableStores.add(convertMerchantStoreToReadableMerchantStore(language, store));
				}
			}
			readableList.setData(readableStores);
			readableList.setRecordsTotal(stores.getTotalElements());
			readableList.setTotalPages(stores.getTotalPages());
			readableList.setNumber(stores.getSize());
			readableList.setRecordsFiltered(stores.getSize());
			return readableList;
		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Error while finding all merchant", e);
		}
	}

	@Override
	public List<ReadableMerchantStore> getMerchantStoreNames(MerchantStoreCriteria criteria) {
		Validate.notNull(criteria, "MerchantStoreCriteria must not be null");
		try {
			Optional<String> code = Optional.ofNullable(criteria.getStoreCode());
			if (code.isPresent()) {
				return merchantStoreService.findAllStoreNames(code.get()).stream()
						.map(this::convertStoreName)
						.collect(Collectors.toList());
			}
			return merchantStoreService.findAllStoreNames().stream()
					.map(this::convertStoreName)
					.collect(Collectors.toList());
		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Exception while getting store name", e);
		}
	}

	@Override
	public List<Language> supportedLanguages(MerchantStore store) {
		Validate.notNull(store, "MerchantStore cannot be null");
		if (!CollectionUtils.isEmpty(store.getLanguages())) {
			return store.getLanguages();
		}
		try {
			MerchantStore refreshed = merchantStoreService.getByCode(store.getCode());
			if (refreshed != null && !CollectionUtils.isEmpty(refreshed.getLanguages())) {
				return refreshed.getLanguages();
			}
		} catch (ServiceException e) {
			throw new ServiceRuntimeException("An exception occured when getting store [" + store.getCode() + "]");
		}
		return Collections.emptyList();
	}

	@Override
	public MerchantStoreSnapshot getSnapshot(String code, Language language) {
		ReadableMerchantStore readable = getFullByCode(code, language);
		return merchantStoreSnapshotPopulator.toSnapshot(readable);
	}

	private Language resolveLanguage(String lang) {
		try {
			if (StringUtils.isNotBlank(lang)) {
				Language language = languageService.getByCode(lang);
				if (language != null) {
					return language;
				}
			}
			return languageService.defaultLanguage();
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e.getMessage(), e);
		}
	}

	private ReadableMerchantStore convertMerchantStoreToReadableMerchantStore(Language language, MerchantStore store) {
		ReadableMerchantStore readable = new ReadableMerchantStore();
		try {
			readableMerchantStorePopulator.populate(store, readable, store, language);
		} catch (Exception e) {
			throw new ConversionRuntimeException("Error while populating MerchantStore " + e.getMessage());
		}
		return readable;
	}

	private MerchantStore getMerchantStoreByCode(String code) {
		return Optional.ofNullable(get(code))
				.orElseThrow(() -> new ResourceNotFoundException("Merchant store code [" + code + "] not found"));
	}

	private void createMerchantStore(MerchantStore mStore) {
		try {
			merchantStoreService.saveOrUpdate(mStore);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e.getMessage(), e);
		}
	}

	private MerchantStore convertPersistableMerchantStoreToMerchantStore(
			PersistableMerchantStore store, Language language) {
		MerchantStore mStore = new MerchantStore();
		mStore.setWeightunitcode(MeasureUnit.KG.name());
		mStore.setSeizeunitcode(MeasureUnit.IN.name());
		try {
			return persistableMerchantStorePopulator.populate(store, mStore, mStore, language);
		} catch (ConversionException e) {
			throw new ConversionRuntimeException(e);
		}
	}

	private void updateMerchantStore(MerchantStore mStore) {
		try {
			merchantStoreService.update(mStore);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e.getMessage(), e);
		}
	}

	private MerchantStore mergePersistableMerchantStoreToMerchantStore(
			PersistableMerchantStore store, String code, Language language) {
		MerchantStore mStore = getMerchantStoreByCode(code);
		store.setId(mStore.getId());
		try {
			return persistableMerchantStorePopulator.populate(store, mStore, mStore, language);
		} catch (ConversionException e) {
			throw new ConversionRuntimeException(e);
		}
	}

	private ReadableMerchantStoreList getMerchantStoresByCriteria(MerchantStoreCriteria criteria, Language language) {
		try {
			GenericEntityList<MerchantStore> stores = Optional.ofNullable(merchantStoreService.getByCriteria(criteria))
					.orElseThrow(() -> new ResourceNotFoundException("Criteria did not match any store"));

			ReadableMerchantStoreList storeList = new ReadableMerchantStoreList();
			storeList.setData(stores.getList().stream()
					.map(s -> convertMerchantStoreToReadableMerchantStore(language, s))
					.collect(Collectors.toList()));
			storeList.setTotalPages(stores.getTotalPages());
			storeList.setRecordsTotal(stores.getTotalCount());
			storeList.setNumber(stores.getList().size());
			return storeList;
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e.getMessage(), e);
		}
	}

	private List<MerchantConfigEntity> getMerchantConfigEntities(MerchantStore mStore) {
		List<MerchantConfiguration> configurations = getMergeConfigurationsByStore(MerchantConfigurationType.SOCIAL, mStore);
		return configurations.stream()
				.map(this::convertToMerchantConfigEntity)
				.collect(Collectors.toList());
	}

	private List<MerchantConfiguration> getMergeConfigurationsByStore(
			MerchantConfigurationType configurationType, MerchantStore mStore) {
		try {
			return merchantConfigurationService.listByType(configurationType, mStore);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Error wile getting merchantConfigurations " + e.getMessage());
		}
	}

	private MerchantConfigEntity convertToMerchantConfigEntity(MerchantConfiguration config) {
		MerchantConfigEntity configTO = new MerchantConfigEntity();
		configTO.setId(config.getId());
		configTO.setKey(config.getKey());
		configTO.setType(config.getMerchantConfigurationType() != null
				? config.getMerchantConfigurationType().name()
				: null);
		configTO.setValue(config.getValue());
		configTO.setActive(config.getActive() != null && config.getActive().booleanValue());
		return configTO;
	}

	private MerchantConfiguration convertToMerchantConfiguration(
			MerchantConfigEntity config, MerchantConfigurationType configurationType) {
		MerchantConfiguration configTO = new MerchantConfiguration();
		configTO.setId(config.getId());
		configTO.setKey(config.getKey());
		configTO.setMerchantConfigurationType(configurationType);
		configTO.setValue(config.getValue());
		configTO.setActive(Boolean.valueOf(config.isActive()));
		return configTO;
	}

	private void saveMerchantStore(MerchantStore store) {
		try {
			merchantStoreService.save(store);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e.getMessage(), e);
		}
	}

	private ReadableMerchantStore convertStoreName(MerchantStore store) {
		ReadableMerchantStore convert = new ReadableMerchantStore();
		convert.setId(store.getId());
		convert.setCode(store.getCode());
		convert.setName(store.getStorename());
		return convert;
	}
}

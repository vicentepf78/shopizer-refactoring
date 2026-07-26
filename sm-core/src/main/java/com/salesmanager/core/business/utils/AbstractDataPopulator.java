/**
 * 
 */
package com.salesmanager.core.business.utils;

import java.util.Locale;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.tenant.TenantEntityBridgeRegistry;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;


/**
 * @author Umesh A
 *
 */
public abstract class AbstractDataPopulator<Source,Target> implements DataPopulator<Source, Target>
{

 
   
    private Locale locale;

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	public Locale getLocale() {
		return locale;
	}
	


	@Override
	public Target populate(Source source, MerchantStore store, Language language) throws ConversionException{
	   return populate(source,createTarget(), store, language);
	}

	@Override
	public Target populate(Source source, MerchantStoreId storeId, LanguageCode languageCode) throws ConversionException {
		return populate(source, createTarget(), storeId, languageCode);
	}

	@Override
	public Target populate(Source source, Target target, MerchantStoreId storeId, LanguageCode languageCode)
			throws ConversionException {
		TenantEntityBridgeRegistry.Bridge bridge = TenantEntityBridgeRegistry.require();
		return populate(source, target, bridge.resolveStore(storeId), bridge.resolveLanguage(languageCode));
	}

	protected abstract Target createTarget();

   

}

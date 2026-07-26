/**
 * 
 */
package com.salesmanager.core.business.utils;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

/**
 * @author Umesh A
 *
 */
public interface DataPopulator<Source,Target> {

    Target populate(Source source,Target target, MerchantStore store, Language language) throws ConversionException;
    Target populate(Source source, MerchantStore store, Language language) throws ConversionException;
    Target populate(Source source, Target target, MerchantStoreId storeId, LanguageCode languageCode) throws ConversionException;
    Target populate(Source source, MerchantStoreId storeId, LanguageCode languageCode) throws ConversionException;

}

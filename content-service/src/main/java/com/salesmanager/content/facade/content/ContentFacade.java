package com.salesmanager.content.facade.content;

import java.util.List;
import java.util.Optional;

import com.salesmanager.contracts.content.ContentFile;
import com.salesmanager.contracts.content.ContentFolder;
import com.salesmanager.contracts.content.ReadableContentEntity;
import com.salesmanager.contracts.content.ReadableContentFull;
import com.salesmanager.contracts.content.box.PersistableContentBox;
import com.salesmanager.contracts.content.box.ReadableContentBox;
import com.salesmanager.contracts.content.page.PersistableContentPage;
import com.salesmanager.contracts.content.page.ReadableContentPage;
import com.salesmanager.contracts.common.ReadableEntityList;
import com.salesmanager.core.model.content.ContentType;
import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.OutputContentFile;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

public interface ContentFacade {

	ContentFolder getContentFolder(String folder, MerchantStore store) throws Exception;

	String absolutePath(MerchantStore store, String file);

	void delete(MerchantStore store, String fileName, String fileType);

	void delete(MerchantStore store, Long id);

	ReadableEntityList<ReadableContentPage> getContentPages(MerchantStore store, Language language, int page, int count);

	ReadableContentPage getContentPage(String code, MerchantStore store, Language language);

	ReadableContentPage getContentPageByName(String name, MerchantStore store, Language language);

	ReadableContentBox getContentBox(String code, MerchantStore store, Language language);

	boolean codeExist(String code, String type, MerchantStore store);

	ReadableEntityList<ReadableContentBox> getContentBoxes(ContentType type, String codePrefix, MerchantStore store,
			Language language, int start, int count);

	ReadableEntityList<ReadableContentBox> getContentBoxes(ContentType type, MerchantStore store, Language language,
			int start, int count);

	void addContentFile(ContentFile file, String merchantStoreCode);

	void addContentFiles(List<ContentFile> file, String merchantStoreCode);

	Long saveContentPage(PersistableContentPage page, MerchantStore merchantStore, Language language);

	void updateContentPage(Long id, PersistableContentPage page, MerchantStore merchantStore, Language language);

	void deleteContent(Long id, MerchantStore merchantStore);

	Long saveContentBox(PersistableContentBox box, MerchantStore merchantStore, Language language);

	void updateContentBox(Long id, PersistableContentBox box, MerchantStore merchantStore, Language language);

	ReadableContentFull getContent(String code, MerchantStore store, Language language);

	List<ReadableContentEntity> getContents(Optional<String> type, MerchantStore store, Language language);

	void renameFile(MerchantStore store, FileContentType fileType, String originalName, String newName);

	OutputContentFile download(MerchantStore store, FileContentType fileType, String fileName);

	byte[] getStaticFile(String storeCode, FileContentType fileType, String fileName);

	void uploadLogo(String storeCode, String fileName, byte[] content, String contentType);

	void deleteLogo(String storeCode, String fileName);
}

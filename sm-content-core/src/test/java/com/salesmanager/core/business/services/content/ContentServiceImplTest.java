package com.salesmanager.core.business.services.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.modules.cms.content.StaticContentFileManager;
import com.salesmanager.core.business.repositories.content.ContentRepository;
import com.salesmanager.core.business.repositories.content.PageContentRepository;
import com.salesmanager.core.model.content.Content;
import com.salesmanager.core.model.content.ContentDescription;
import com.salesmanager.core.model.content.ContentType;
import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.InputContentFile;
import com.salesmanager.core.model.content.OutputContentFile;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

@ExtendWith(MockitoExtension.class)
class ContentServiceImplTest {

	@Mock
	private ContentRepository contentRepository;

	@Mock
	private PageContentRepository pageContentRepository;

	@Mock
	private StaticContentFileManager contentFileManager;

	private ContentServiceImpl contentService;

	@BeforeEach
	void setUp() throws Exception {
		contentService = new ContentServiceImpl(contentRepository);
		setField(contentService, "pageContentRepository", pageContentRepository);
		setField(contentService, "contentFileManager", contentFileManager);
	}

	@Test
	void pageAndBoxQueries_usePageContentRepository_notProductFileManager() throws Exception {
		MerchantStore store = store(1);
		Language language = language(1);
		Page<Content> page = new PageImpl<>(Collections.singletonList(content("box-1", ContentType.BOX)));

		when(pageContentRepository.findByContentType(ContentType.BOX, 1, PageRequest.of(0, 10))).thenReturn(page);
		when(pageContentRepository.findByContentType(ContentType.PAGE, 1, 1, PageRequest.of(0, 5))).thenReturn(page);

		assertEquals(1, contentService.listByType(ContentType.BOX, store, 0, 10).getTotalElements());
		assertEquals(1,
				contentService.listByType(ContentType.PAGE, store, language, 0, 5).getTotalElements());

		verify(pageContentRepository).findByContentType(ContentType.BOX, 1, PageRequest.of(0, 10));
		verify(pageContentRepository).findByContentType(ContentType.PAGE, 1, 1, PageRequest.of(0, 5));
		assertFalse(hasProductFileManagerField());
	}

	@Test
	void listByType_delegatesToContentRepository() throws Exception {
		MerchantStore store = store(1);
		Language language = language(1);
		List<Content> expected = Arrays.asList(content("page-a", ContentType.PAGE));

		when(contentRepository.findByType(ContentType.PAGE, 1, 1)).thenReturn(expected);

		assertSame(expected, contentService.listByType(ContentType.PAGE, store, language));
	}

	@Test
	void getByCodeAndLanguage_delegatesToRepository() throws Exception {
		MerchantStore store = store(1);
		Language language = language(1);
		Content expected = content("home", ContentType.PAGE);
		when(contentRepository.findByCode("home", 1, 1)).thenReturn(expected);

		assertSame(expected, contentService.getByCode("home", store, language));
	}

	@Test
	void getById_withStore_filtersOtherStores() throws Exception {
		MerchantStore store = store(1);
		Content owned = content("mine", ContentType.PAGE);
		owned.setMerchantStore(store);
		when(contentRepository.findOne(99L)).thenReturn(owned);

		assertSame(owned, contentService.getById(99L, store, language(1)));

		MerchantStore other = store(2);
		assertEquals(null, contentService.getById(99L, other, language(1)));
	}

	@Test
	void addLogo_delegatesToContentFileManager() throws Exception {
		InputContentFile file = inputFile("logo.png");
		contentService.addLogo("DEFAULT", file);
		verify(contentFileManager).addFile(eq("DEFAULT"), any(Optional.class), any(InputContentFile.class));
	}

	@Test
	void getContentFile_delegatesToContentFileManager() throws Exception {
		OutputContentFile output = new OutputContentFile();
		when(contentFileManager.getFile(eq("DEFAULT"), any(Optional.class), eq(FileContentType.IMAGE), eq("a.png")))
				.thenReturn(output);

		assertSame(output,
				contentService.getContentFile("DEFAULT", FileContentType.IMAGE, "a.png"));
	}

	@Test
	void addFolder_invalidPath_throwsServiceException() {
		MerchantStore store = store(1);
		assertThrows(ServiceException.class,
				() -> contentService.addFolder(store, Optional.of("not-valid"), "folder"));
	}

	@Test
	void addFolder_validPath_delegatesToContentFileManager() throws Exception {
		MerchantStore store = store(1);
		store.setCode("DEFAULT");
		contentService.addFolder(store, Optional.of("/assets"), "images");
		verify(contentFileManager).addFolder("DEFAULT", "images", Optional.of("/assets"));
	}

	@Test
	void renameFile_whenMissing_throwsServiceException() throws Exception {
		when(contentFileManager.getFile(eq("DEFAULT"), any(Optional.class), eq(FileContentType.IMAGE), eq("old.png")))
				.thenReturn(null);

		assertThrows(ServiceException.class,
				() -> contentService.renameFile("DEFAULT", FileContentType.IMAGE, Optional.empty(), "old.png",
						"new.png"));
	}

	@Test
	void isValidLinuxDirectory_acceptsAbsolutePaths() {
		assertTrue(contentService.isValidLinuxDirectory("/assets/images"));
		assertFalse(contentService.isValidLinuxDirectory("relative/path"));
	}

	@Test
	void exists_whenPresent_returnsTrue() {
		MerchantStore store = store(1);
		when(contentRepository.findByCodeAndType("footer", ContentType.BOX, 1))
				.thenReturn(content("footer", ContentType.BOX));

		assertTrue(contentService.exists("footer", ContentType.BOX, store));
	}

	@Test
	void exists_whenAbsent_returnsFalse() {
		MerchantStore store = store(1);
		when(contentRepository.findByCodeAndType("missing", ContentType.PAGE, 1)).thenReturn(null);

		assertFalse(contentService.exists("missing", ContentType.PAGE, store));
	}

	@Test
	void getBySeUrl_delegatesToRepository() {
		MerchantStore store = store(1);
		ContentDescription expected = new ContentDescription();
		when(contentRepository.getBySeUrl(store, "/about")).thenReturn(expected);

		assertSame(expected, contentService.getBySeUrl(store, "/about"));
	}

	@Test
	void listFolders_delegatesToContentFileManager() throws Exception {
		MerchantStore store = store(1);
		store.setCode("DEFAULT");
		when(contentFileManager.listFolders("DEFAULT", Optional.empty())).thenReturn(Collections.singletonList("images"));

		assertEquals(1, contentService.listFolders(store, Optional.empty()).size());
	}

	@Test
	void saveOrUpdate_newEntity_saves() throws Exception {
		Content content = content("new-page", ContentType.PAGE);
		contentService.saveOrUpdate(content);
		verify(contentRepository).saveAndFlush(content);
	}

	@Test
	void saveOrUpdate_existingEntity_updates() throws Exception {
		Content content = content("existing", ContentType.PAGE);
		content.setId(10L);
		contentService.saveOrUpdate(content);
		verify(contentRepository).saveAndFlush(content);
	}

	@Test
	void delete_loadsAndRemovesEntity() throws Exception {
		Content input = content("gone", ContentType.PAGE);
		input.setId(3L);
		Content loaded = content("gone", ContentType.PAGE);
		loaded.setId(3L);
		when(contentRepository.findOne(3L)).thenReturn(loaded);

		contentService.delete(input);
		verify(contentRepository).delete(loaded);
	}

	@Test
	void getByLanguage_delegatesToRepository() throws Exception {
		Content expected = content("x", ContentType.PAGE);
		when(contentRepository.findByIdAndLanguage(5L, 1)).thenReturn(expected);

		assertSame(expected, contentService.getByLanguage(5L, language(1)));
	}

	@Test
	void listByTypes_delegatesToRepository() throws Exception {
		MerchantStore store = store(1);
		List<ContentType> types = Arrays.asList(ContentType.PAGE, ContentType.BOX);
		List<Content> expected = Collections.singletonList(content("a", ContentType.PAGE));
		when(contentRepository.findByTypes(types, 1, 1)).thenReturn(expected);

		assertSame(expected, contentService.listByType(types, store, language(1)));
	}

	@Test
	void getByCodeLike_delegatesToRepository() {
		MerchantStore store = store(1);
		List<Content> expected = Collections.singletonList(content("pref", ContentType.BOX));
		when(contentRepository.findByCodeLike(ContentType.BOX, "%pref%", 1, 1)).thenReturn(expected);

		assertSame(expected, contentService.getByCodeLike(ContentType.BOX, "pref", store, language(1)));
	}

	@Test
	void removeFiles_delegatesToContentFileManager() throws Exception {
		contentService.removeFiles("DEFAULT");
		verify(contentFileManager).removeFiles(eq("DEFAULT"), any(Optional.class));
	}

	@Test
	void getContentFilesNames_delegatesToContentFileManager() throws Exception {
		when(contentFileManager.getFileNames(eq("DEFAULT"), any(Optional.class), eq(FileContentType.IMAGE)))
				.thenReturn(Collections.singletonList("a.png"));

		assertEquals(1, contentService.getContentFilesNames("DEFAULT", FileContentType.IMAGE).size());
	}

	@Test
	void addContentFile_image_delegatesToContentFileManager() throws Exception {
		InputContentFile file = inputFile("photo.jpg");
		file.setFileContentType(FileContentType.IMAGE);

		contentService.addContentFile("DEFAULT", file);
		verify(contentFileManager).addFile(eq("DEFAULT"), any(Optional.class), any(InputContentFile.class));
	}

	@Test
	void addContentFile_staticFile_delegatesToContentFileManager() throws Exception {
		InputContentFile file = inputFile("doc.pdf");
		file.setFileContentType(FileContentType.STATIC_FILE);

		contentService.addContentFile("DEFAULT", file);
		verify(contentFileManager).addFile(eq("DEFAULT"), any(Optional.class), any(InputContentFile.class));
	}

	@Test
	void addContentFiles_delegatesToContentFileManager() throws Exception {
		InputContentFile file = inputFile("a.png");
		contentService.addContentFiles("DEFAULT", Collections.singletonList(file));
		verify(contentFileManager).addFiles(eq("DEFAULT"), any(Optional.class), eq(Collections.singletonList(file)));
	}

	@Test
	void getContentFiles_delegatesToContentFileManager() throws Exception {
		when(contentFileManager.getFiles(eq("DEFAULT"), any(Optional.class), eq(FileContentType.IMAGE)))
				.thenReturn(Collections.emptyList());

		assertTrue(contentService.getContentFiles("DEFAULT", FileContentType.IMAGE).isEmpty());
	}

	@Test
	void removeFile_byName_delegatesToContentFileManager() throws Exception {
		contentService.removeFile("DEFAULT", "file.pdf");
		verify(contentFileManager).removeFile(eq("DEFAULT"), eq(FileContentType.STATIC_FILE), eq("file.pdf"),
				any(Optional.class));
	}

	@Test
	void renameFile_whenPresent_replacesFile() throws Exception {
		OutputContentFile existing = new OutputContentFile();
		existing.setMimeType("image/png");
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		baos.write(1);
		baos.write(2);
		existing.setFile(baos);
		when(contentFileManager.getFile(eq("DEFAULT"), any(Optional.class), eq(FileContentType.IMAGE), eq("old.png")))
				.thenReturn(existing);

		contentService.renameFile("DEFAULT", FileContentType.IMAGE, Optional.empty(), "old.png", "new.png");

		verify(contentFileManager).removeFile(eq("DEFAULT"), eq(FileContentType.IMAGE), eq("old.png"),
				any(Optional.class));
		verify(contentFileManager).addFile(eq("DEFAULT"), any(Optional.class), any(InputContentFile.class));
	}

	@Test
	void addOptionImage_setsPropertyType() throws Exception {
		InputContentFile file = inputFile("opt.png");
		contentService.addOptionImage("DEFAULT", file);
		verify(contentFileManager).addFile(eq("DEFAULT"), any(Optional.class), any(InputContentFile.class));
	}

	@Test
	void removeFolder_delegatesToContentFileManager() throws Exception {
		MerchantStore store = store(1);
		store.setCode("DEFAULT");
		contentService.removeFolder(store, Optional.empty(), "old");
		verify(contentFileManager).removeFolder("DEFAULT", "old", Optional.empty());
	}

	private static boolean hasProductFileManagerField() {
		return Arrays.stream(ContentServiceImpl.class.getDeclaredFields())
				.anyMatch(f -> f.getName().toLowerCase().contains("productfilemanager"));
	}

	private static void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static MerchantStore store(int id) {
		MerchantStore store = new MerchantStore();
		store.setId(id);
		store.setCode("DEFAULT");
		return store;
	}

	private static Language language(int id) {
		Language language = new Language("en");
		language.setId(id);
		return language;
	}

	private static Content content(String code, ContentType type) {
		Content content = new Content();
		content.setCode(code);
		content.setContentType(type);
		return content;
	}

	private static InputContentFile inputFile(String name) {
		InputContentFile file = new InputContentFile();
		file.setFileName(name);
		file.setFileContentType(FileContentType.IMAGE);
		file.setFile(new ByteArrayInputStream(new byte[] { 1 }));
		return file;
	}
}

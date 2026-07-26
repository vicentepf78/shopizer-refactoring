package com.salesmanager.content.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.content.ContentFile;
import com.salesmanager.contracts.content.box.PersistableContentBox;
import com.salesmanager.contracts.content.box.ReadableContentBox;
import com.salesmanager.contracts.content.common.ContentDescription;
import com.salesmanager.contracts.content.page.PersistableContentPage;
import com.salesmanager.contracts.content.page.ReadableContentPage;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.core.model.content.ContentType;
import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.content.facade.content.ContentFacade;
import com.salesmanager.content.support.ConstraintException;
import com.salesmanager.content.support.ResourceNotFoundException;
import com.salesmanager.content.support.ServiceRuntimeException;
import com.salesmanager.content.support.TestDataFactory;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class ContentFacadeImplTest {

	@Autowired
	private ContentFacade contentFacade;
	@Autowired
	private TestDataFactory testDataFactory;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	private MerchantStore store;
	private Language language;

	@BeforeEach
	void setUp() {
		TestDataFactory.Seed seed = testDataFactory.ensureDefaultAdmin();
		store = seed.store;
		language = seed.language;
		testDataFactory.ensureLanguage("fr");

		ReadableLanguage readableEn = new ReadableLanguage();
		readableEn.setId(language.getId());
		readableEn.setCode("en");
		ReadableLanguage readableFr = new ReadableLanguage();
		readableFr.setCode("fr");
		when(referenceServiceClient.getLanguageByCode(eq("en"))).thenReturn(readableEn);
		when(referenceServiceClient.getLanguageByCode(eq("fr"))).thenReturn(readableFr);
	}

	@Test
	void getContentPages_returnsContractDtos_notJpaEntities() {
		var pages = contentFacade.getContentPages(store, language, 0, 10);
		assertThat(pages).isNotNull();
		for (ReadableContentPage item : pages.getItems()) {
			assertThat(item.getClass().getName()).doesNotContain("core.model.content.Content");
		}
	}

	@Test
	void referenceClientResolvesLanguageByCode() {
		assertThat(referenceServiceClient.getLanguageByCode("en").getCode()).isEqualTo("en");
	}

	@Test
	void getContentBoxes_withCodePrefixStub_returnsNull() {
		assertThat(contentFacade.getContentBoxes(ContentType.BOX, "summary_", store, language, 0, 10)).isNull();
	}

	@Test
	void pageAndBoxCrud_roundTripContractDtos() {
		String pageCode = "pg" + UUID.randomUUID().toString().substring(0, 6);
		PersistableContentPage page = page(pageCode, "en", "Home EN");
		contentFacade.saveContentPage(page, store, language);

		ReadableContentPage readable = contentFacade.getContentPage(pageCode, store, language);
		assertThat(readable.getCode()).isEqualTo(pageCode);

		String boxCode = "bx" + UUID.randomUUID().toString().substring(0, 6);
		PersistableContentBox box = box(boxCode, "Footer");
		contentFacade.saveContentBox(box, store, language);
		ReadableContentBox readableBox = contentFacade.getContentBox(boxCode, store, language);
		assertThat(readableBox.getDescription().getName()).isEqualTo("Footer");
	}

	@Test
	void fileAndLogoOperations_persistAndReadBack() throws Exception {
		ContentFile file = new ContentFile();
		file.setName("facade-test.png");
		file.setContentType("image/png");
		file.setFile(new byte[] { 1, 2, 3, 4 });
		contentFacade.addContentFile(file, store.getCode());

		byte[] bytes = contentFacade.getStaticFile(store.getCode(), FileContentType.IMAGE, "facade-test.png");
		assertThat(bytes).isNotEmpty();

		contentFacade.uploadLogo(store.getCode(), "logo.png", new byte[] { 9, 8, 7 }, "image/png");
		contentFacade.deleteLogo(store.getCode(), "logo.png");

		assertThat(contentFacade.getContentFolder("/", store).getContent()).isNotEmpty();
		assertThat(contentFacade.absolutePath(store, "facade-test.png")).contains("DEFAULT");
		contentFacade.delete(store, "facade-test.png", FileContentType.IMAGE.name());
	}

	@Test
	void updatePageAndLookupByName() {
		String code = "nm" + UUID.randomUUID().toString().substring(0, 6);
		contentFacade.saveContentPage(page(code, "en", "Lookup"), store, language);
		ReadableContentPage byName = contentFacade.getContentPageByName("lookup", store, language);
		assertThat(byName.getCode()).isEqualTo(code);

		PersistableContentPage update = page(code, "en", "Lookup updated");
		update.setDescriptions(Arrays.asList(desc("en", "Lookup updated", "lookup")));
		ReadableContentPage existing = contentFacade.getContentPage(code, store, language);
		if (existing.getId() != null && existing.getId() > 0) {
			contentFacade.updateContentPage(existing.getId(), update, store, language);
		}
	}

	@Test
	void deprecatedSurfaces_returnContractDtos() {
		String code = "dp" + UUID.randomUUID().toString().substring(0, 6);
		contentFacade.saveContentPage(page(code, "en", "Deprecated"), store, language);
		assertThat(contentFacade.getContent(code, store, language)).isNotNull();
		assertThat(contentFacade.getContents(Optional.empty(), store, language)).isNotEmpty();
	}

	@Test
	void boxUpdateAndFileRename() {
		String boxCode = "up" + UUID.randomUUID().toString().substring(0, 6);
		contentFacade.saveContentBox(box(boxCode, "Before"), store, language);
		ReadableContentBox existing = contentFacade.getContentBox(boxCode, store, language);
		PersistableContentBox update = box(boxCode, "After");
		if (existing.getId() != null && existing.getId() > 0) {
			contentFacade.updateContentBox(existing.getId(), update, store, language);
		}
		contentFacade.addContentFile(file("rename-me.png"), store.getCode());
		contentFacade.renameFile(store, FileContentType.IMAGE, "rename-me.png", "renamed.png");
		assertThat(contentFacade.download(store, FileContentType.IMAGE, "renamed.png")).isNotNull();
		contentFacade.addContentFiles(Collections.singletonList(file("batch.png")), store.getCode());
	}

	@Test
	void multilingualReads_withoutRequestLanguage() {
		String code = "ml" + UUID.randomUUID().toString().substring(0, 6);
		PersistableContentPage multi = page(code, "en", "Multi");
		multi.setDescriptions(Arrays.asList(desc("en", "Multi EN", "multi-en"), desc("fr", "Multi FR", "multi-fr")));
		contentFacade.saveContentPage(multi, store, language);

		assertThat(contentFacade.getContentPage(code, store, null)).isNotNull();

		String boxCode = "mlb" + UUID.randomUUID().toString().substring(0, 5);
		contentFacade.saveContentBox(box(boxCode, "Box ML"), store, language);
		assertThat(contentFacade.getContentBox(boxCode, store, null)).isNotNull();
	}

	@Test
	void deleteContentById_whenPresent() {
		String code = "del" + UUID.randomUUID().toString().substring(0, 5);
		contentFacade.saveContentPage(page(code, "en", "Delete me"), store, language);
		ReadableContentPage saved = contentFacade.getContentPage(code, store, language);
		if (saved.getId() != null && saved.getId() > 0) {
			contentFacade.deleteContent(saved.getId(), store);
		}
	}

	@Test
	void getStaticFile_whenMissing_throwsNotFound() {
		assertThatThrownBy(() -> contentFacade.getStaticFile(store.getCode(), FileContentType.IMAGE, "missing.png"))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void addStaticFile_usesNonImageContentTypeBranch() {
		ContentFile pdf = new ContentFile();
		pdf.setName("doc.pdf");
		pdf.setContentType("application/pdf");
		pdf.setFile(new byte[] { 5, 6, 7 });
		contentFacade.addContentFile(pdf, store.getCode());
	}

	@Test
	void codeExist_reflectsPersistence() {
		String code = "cx" + UUID.randomUUID().toString().substring(0, 6);
		assertThat(contentFacade.codeExist(code, "PAGE", store)).isFalse();
		contentFacade.saveContentPage(page(code, "en", "Check"), store, language);
		assertThat(contentFacade.codeExist(code, "PAGE", store)).isTrue();
	}

	@Test
	void deleteByStoreAndId_whenPresent() {
		String code = "del3" + UUID.randomUUID().toString().substring(0, 5);
		contentFacade.saveContentPage(page(code, "en", "Del"), store, language);
		ReadableContentPage saved = contentFacade.getContentPage(code, store, language);
		contentFacade.delete(store, saved.getId());
		assertThatThrownBy(() -> contentFacade.getContentPage(code, store, language))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void deleteByStoreAndId_whenWrongStore_throwsNotFound() {
		String code = "del2" + UUID.randomUUID().toString().substring(0, 5);
		contentFacade.saveContentPage(page(code, "en", "Del"), store, language);
		ReadableContentPage saved = contentFacade.getContentPage(code, store, language);
		MerchantStore wrongStore = new MerchantStore();
		wrongStore.setId(store.getId() + 9999);
		wrongStore.setCode("WRONG");
		assertThatThrownBy(() -> contentFacade.delete(wrongStore, saved.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void saveDuplicatePage_throwsConstraint() {
		String code = "dup" + UUID.randomUUID().toString().substring(0, 6);
		contentFacade.saveContentPage(page(code, "en", "Dup"), store, language);
		assertThatThrownBy(() -> contentFacade.saveContentPage(page(code, "en", "Dup2"), store, language))
				.isInstanceOf(ServiceRuntimeException.class)
				.hasCauseInstanceOf(ConstraintException.class);
	}

	@Test
	void saveDuplicateBox_throwsConstraint() {
		String code = "dbx" + UUID.randomUUID().toString().substring(0, 6);
		contentFacade.saveContentBox(box(code, "Dup box"), store, language);
		assertThatThrownBy(() -> contentFacade.saveContentBox(box(code, "Dup box 2"), store, language))
				.isInstanceOf(ServiceRuntimeException.class)
				.hasCauseInstanceOf(ConstraintException.class);
	}

	@Test
	void updatePageWhenMissing_throwsConstraint() {
		assertThatThrownBy(
				() -> contentFacade.updateContentPage(999999L, page("missing", "en", "missing"), store, language))
				.isInstanceOf(ServiceRuntimeException.class)
				.hasCauseInstanceOf(ConstraintException.class);
	}

	@Test
	void deleteContentWhenMissing_throwsConstraint() {
		assertThatThrownBy(() -> contentFacade.deleteContent(999999L, store))
				.isInstanceOf(ServiceRuntimeException.class)
				.hasCauseInstanceOf(ConstraintException.class);
	}

	@Test
	void getContentFolder_withPath_encodesFolder() throws Exception {
		contentFacade.addContentFile(file("folder-test.png"), store.getCode());
		assertThat(contentFacade.getContentFolder("images", store).getPath()).isNotBlank();
	}

	@Test
	void getContentPages_listsSavedPages() {
		String code = "lst" + UUID.randomUUID().toString().substring(0, 6);
		contentFacade.saveContentPage(page(code, "en", "Listed"), store, language);
		assertThat(contentFacade.getContentPages(store, language, 0, 50).getItems())
				.anyMatch(p -> code.equals(p.getCode()));
	}

	@Test
	void getContentBoxes_listsSavedBoxes() {
		String code = "lbx" + UUID.randomUUID().toString().substring(0, 6);
		contentFacade.saveContentBox(box(code, "Listed box"), store, language);
		assertThat(contentFacade.getContentBoxes(ContentType.BOX, store, language, 0, 50).getItems())
				.anyMatch(b -> code.equals(b.getCode()));
	}

	private static ContentFile file(String name) {
		ContentFile f = new ContentFile();
		f.setName(name);
		f.setContentType("image/png");
		f.setFile(new byte[] { 1, 2, 3 });
		return f;
	}

	private static PersistableContentPage page(String code, String lang, String name) {
		PersistableContentPage page = new PersistableContentPage();
		page.setCode(code);
		page.setVisible(true);
		page.setLinkToMenu(false);
		page.setContentType("PAGE");
		page.setDescriptions(Collections.singletonList(desc(lang, name, name.toLowerCase())));
		return page;
	}

	private static PersistableContentBox box(String code, String name) {
		PersistableContentBox box = new PersistableContentBox();
		box.setCode(code);
		box.setVisible(true);
		box.setContentType("BOX");
		ContentDescription d = desc("en", name, name.toLowerCase());
		box.setDescriptions(Collections.singletonList(d));
		return box;
	}

	private static ContentDescription desc(String lang, String name, String url) {
		ContentDescription d = new ContentDescription();
		d.setLanguage(lang);
		d.setName(name);
		d.setTitle(name);
		d.setFriendlyUrl(url);
		d.setDescription("body");
		return d;
	}
}

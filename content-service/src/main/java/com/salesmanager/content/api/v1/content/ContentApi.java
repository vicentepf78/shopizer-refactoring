package com.salesmanager.content.api.v1.content;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.salesmanager.core.model.content.ContentType;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.contracts.content.ContentFile;
import com.salesmanager.contracts.content.ContentFolder;
import com.salesmanager.contracts.content.ContentName;
import com.salesmanager.contracts.content.PersistableContentEntity;
import com.salesmanager.contracts.content.ReadableContentEntity;
import com.salesmanager.contracts.content.ReadableContentFull;
import com.salesmanager.contracts.content.box.PersistableContentBox;
import com.salesmanager.contracts.content.box.ReadableContentBox;
import com.salesmanager.contracts.content.page.PersistableContentPage;
import com.salesmanager.contracts.content.page.ReadableContentPage;
import com.salesmanager.contracts.common.Entity;
import com.salesmanager.contracts.common.EntityExists;
import com.salesmanager.contracts.common.ReadableEntityList;
import com.salesmanager.content.support.ServiceRuntimeException;
import com.salesmanager.content.facade.content.ContentFacade;
import com.salesmanager.content.util.ContentImagePath;


@RestController
@RequestMapping(value = "/api/v1")
public class ContentApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentApi.class);

	private static final String DEFAULT_PATH = "/";
	
	private final static String BOX = "BOX";
	private final static String PAGE = "PAGE";

	@Inject
	private ContentFacade contentFacade;

	@Inject
	private ContentImagePath imageUtils;

	/**
	 * List content pages
	 * @param merchantStore
	 * @param language
	 * @param page
	 * @param count
	 * @return
	 */
	@GetMapping(value = {"/private/content/pages", "/content/pages"}, produces = MediaType.APPLICATION_JSON_VALUE)
			public ReadableEntityList<ReadableContentPage> pages(
			MerchantStore merchantStore,
			Language language,
			int page,
			int count) {
		return contentFacade
				.getContentPages(merchantStore, language, page, count);
	}

	@Deprecated
	@GetMapping(value = "/content/summary", produces = MediaType.APPLICATION_JSON_VALUE)
			public List<ReadableContentBox> pagesSummary(
			MerchantStore merchantStore, 
			Language language) {
		//return contentFacade.getContentBoxes(ContentType.BOX, "summary_", merchantStore, language);
		return null;
	}

	/**
	 * List all boxes
	 * 
	 * @param merchantStore
	 * @param language
	 * @return
	 */
	@GetMapping(value = {"/content/boxes","/private/content/boxes"}, produces = MediaType.APPLICATION_JSON_VALUE)
			public ReadableEntityList<ReadableContentBox> boxes(
			MerchantStore merchantStore,
			Language language,
			int page,
			int count
			) {
		return contentFacade.getContentBoxes(ContentType.BOX, merchantStore, language, page, count);
	}

	/**
	 * List specific content box
	 * @param code
	 * @param merchantStore
	 * @param language
	 * @return
	 */
	@GetMapping(value = "/content/pages/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
			public ReadableContentPage page(@PathVariable("code") String code, MerchantStore merchantStore,
			Language language) {

		return contentFacade.getContentPage(code, merchantStore, language);

	}

	/**
	 * Get content page by name
	 * @param name
	 * @param merchantStore
	 * @param language
	 * @return
	 */
	@GetMapping(value = "/content/pages/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
			public ReadableContentPage pageByName(@PathVariable("name") String name, MerchantStore merchantStore,
			Language language) {

		return contentFacade.getContentPageByName(name, merchantStore, language);

	}
	
	/**
	 * Create content box
	 * 
	 * @param page
	 * @param merchantStore
	 * @param language
	 * @param pageCode
	 */
	@PostMapping(value = "/private/content/box")
	@ResponseStatus(HttpStatus.CREATED)
			public Entity createBox(
			@RequestBody @Valid PersistableContentBox box, 
			MerchantStore merchantStore,
			Language language) {

		Long id = contentFacade.saveContentBox(box, merchantStore, language);
		Entity entity = new Entity();
		entity.setId(id);
		return entity;
	}
	
	@GetMapping(value = "/private/content/box/{code}/exists")
	@ResponseStatus(HttpStatus.OK)
			public EntityExists boxExists(
			@PathVariable String code, 
			MerchantStore merchantStore,
			Language language) {

		boolean exists = contentFacade.codeExist(code, BOX, merchantStore);
		EntityExists entity = new EntityExists(exists);
		return entity;
	}
	
	@GetMapping(value = "/private/content/page/{code}/exists")
	@ResponseStatus(HttpStatus.OK)
			public EntityExists pageExists(
			@PathVariable String code, 
			MerchantStore merchantStore,
			Language language) {

		boolean exists = contentFacade.codeExist(code, PAGE, merchantStore);
		EntityExists entity = new EntityExists(exists);
		return entity;
	}
	
	/**
	 * Create content page
	 * @param page
	 * @param merchantStore
	 * @param language
	 */
	@PostMapping(value = "/private/content/page")
	@ResponseStatus(HttpStatus.CREATED)
			public Entity createPage(
			@RequestBody @Valid PersistableContentPage page, 
			MerchantStore merchantStore,
			Language language) {

		Long id = contentFacade.saveContentPage(page, merchantStore, language);
		Entity entity = new Entity();
		entity.setId(id);
		return entity;
	}
	
	
	/**
	 * Delete content page
	 * @param id
	 * @param merchantStore
	 * @param language
	 */
	@DeleteMapping(value = "/private/content/page/{id}")
	@ResponseStatus(HttpStatus.OK)
			public void deletePage(
			@PathVariable Long id,
			MerchantStore merchantStore,
			Language language) {

		contentFacade.delete(merchantStore, id);

	}
	
	/**
	 * Delete content box
	 * @param id
	 * @param merchantStore
	 * @param language
	 */
	@DeleteMapping(value = "/private/content/box/{id}")
	@ResponseStatus(HttpStatus.OK)
			public void deleteBox(
			@PathVariable Long id,
			MerchantStore merchantStore,
			Language language) {

		contentFacade.delete(merchantStore, id);

	}
	
	@PutMapping(value = "/private/content/page/{id}")
	@ResponseStatus(HttpStatus.OK)
			public void updatePage(
			@RequestBody @Valid PersistableContentPage page,
			@PathVariable Long id,
			MerchantStore merchantStore,
			Language language) {

		contentFacade.updateContentPage(id, page, merchantStore, language);
	}
	
	@PutMapping(value = "/private/content/box/{id}")
	@ResponseStatus(HttpStatus.OK)
			public void updateBox(
			@RequestBody @Valid PersistableContentBox box,
			@PathVariable Long id,
			MerchantStore merchantStore,
			Language language) {

		contentFacade.updateContentBox(id, box, merchantStore, language);
	}

	@Deprecated
	@GetMapping(value = "/private/content/any/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
			public ReadableContentFull content(@PathVariable("code") String code, MerchantStore merchantStore,
			Language language) {

		return contentFacade.getContent(code, merchantStore, language);

	}

	@Deprecated
	@GetMapping(value = "/private/contents/any", produces = MediaType.APPLICATION_JSON_VALUE)
			public List<ReadableContentEntity> contents(MerchantStore merchantStore, Language language) {

		Optional<String> op = Optional.empty();
		return contentFacade.getContents(op, merchantStore, language);

	}
	
	
	@GetMapping(value = "/private/content/boxes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
			public ReadableContentBox manageBoxByCode(@PathVariable("code") String code, MerchantStore merchantStore,
			Language language) {
		return contentFacade.getContentBox(code, merchantStore, language);
	}

	@GetMapping(value = "/content/boxes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
			public ReadableContentBox getBoxByCode(@PathVariable("code") String code, MerchantStore merchantStore,
			Language language) {
		return contentFacade.getContentBox(code, merchantStore, language);
	}





	/**
	 * 
	 * @param parent
	 * @param folder
	 * @param merchantStore
	 * @param language
	 */
	@DeleteMapping(value = "/content/folder", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
		public void addFolder(@RequestParam String parent, @RequestParam String folder,
			MerchantStore merchantStore, Language language) {

	}

	/**
	 * @param code
	 * @param path
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/content/images", produces = MediaType.APPLICATION_JSON_VALUE)
			public ContentFolder images(MerchantStore merchantStore, Language language,
			@RequestParam(value = "path", required = false) String path, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		//String decodedPath = decodeContentPath(path);
		ContentFolder folder = contentFacade.getContentFolder(path, merchantStore);
		return folder;
	}



	/**
	 * Need type, name and entity
	 *
	 * @param file
	 */
	@PostMapping(value = "/private/file")
	@ResponseStatus(HttpStatus.CREATED)
		public void upload(@RequestParam("file") MultipartFile file, MerchantStore merchantStore,
			Language language) {

		ContentFile f = new ContentFile();
		f.setContentType(file.getContentType());
		f.setName(file.getOriginalFilename());
		try {
			f.setFile(file.getBytes());
		} catch (IOException e) {
			throw new ServiceRuntimeException("Error while getting file bytes");
		}

		contentFacade.addContentFile(f, merchantStore.getCode());

	}

	@PostMapping(value = "/private/files", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
		public void uploadMultipleFiles(@RequestParam(value = "file[]", required = true) MultipartFile[] files,
			MerchantStore merchantStore, Language language) {

		for (MultipartFile f : files) {
			ContentFile cf = new ContentFile();
			cf.setContentType(f.getContentType());
			cf.setName(f.getName());
			try {
				cf.setFile(f.getBytes());
				contentFacade.addContentFile(cf, merchantStore.getCode());
			} catch (IOException e) {
				throw new ServiceRuntimeException("Error while getting file bytes");
			}
		}

	}

	
	@Deprecated
	@PutMapping(value = "/private/content/{id}")
	@ResponseStatus(HttpStatus.OK)
		
	public void updatePage(@PathVariable Long id, @RequestBody @Valid PersistableContentEntity page,
			MerchantStore merchantStore, Language language) {
		page.setId(id);
		//contentFacade.saveContentPage(page, merchantStore, language);
	}

	/**
	 * Deletes a content from CMS
	 *
	 * @param name
	 */
	@Deprecated
	@DeleteMapping(value = "/private/content/{id}")
			public void deleteContent(Long id, MerchantStore merchantStore) {
		contentFacade.delete(merchantStore, id);
	}

	/*  *//**
			 * Deletes a content from CMS
			 *
			 * @param name
			 *//*
			 * @DeleteMapping(value = "/private/content/page/{id}")
			 * 
			 * 			 * 
			 * @ApiImplicitParams({
			 * 
			 * }) public void deleteFile( Long id,
			 * 
			 * MerchantStore merchantStore) {
			 * contentFacade.deletePage(merchantStore, id); }
			 */

	/**
	 * Deletes a file from CMS
	 *
	 * @param name
	 */
	@DeleteMapping(value = "/private/content/")
			public void deleteFile(@Valid ContentName name, MerchantStore merchantStore,
			Language language) {
		contentFacade.delete(merchantStore, name.getName(), name.getContentType());
	}


}

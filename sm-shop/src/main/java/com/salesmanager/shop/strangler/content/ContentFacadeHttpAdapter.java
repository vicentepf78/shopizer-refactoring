package com.salesmanager.shop.strangler.content;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.salesmanager.core.model.content.ContentType;
import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.OutputContentFile;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.content.ContentFile;
import com.salesmanager.shop.model.content.ContentFolder;
import com.salesmanager.shop.model.content.ContentName;
import com.salesmanager.shop.model.content.ReadableContentEntity;
import com.salesmanager.shop.model.content.ReadableContentFull;
import com.salesmanager.shop.model.content.box.PersistableContentBox;
import com.salesmanager.shop.model.content.box.ReadableContentBox;
import com.salesmanager.shop.model.content.page.PersistableContentPage;
import com.salesmanager.shop.model.content.page.ReadableContentPage;
import com.salesmanager.shop.model.entity.Entity;
import com.salesmanager.shop.model.entity.EntityExists;
import com.salesmanager.shop.model.entity.ReadableEntityList;
import com.salesmanager.shop.store.controller.content.facade.ContentFacade;
import com.salesmanager.shop.strangler.support.StranglerRestClient;
import com.salesmanager.shop.utils.ImageFilePath;

@Service("contentFacade")
@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
public class ContentFacadeHttpAdapter implements ContentFacade {

	private static final String BOX = "BOX";
	private static final String PAGE = "PAGE";

	private final StranglerRestClient restClient;
	private final RestTemplate wave2RestTemplate;
	private final String baseUrl;
	private final ImageFilePath imageUtils;
	private final StaticContentProxy staticContentProxy;

	public ContentFacadeHttpAdapter(
			RestTemplate wave2RestTemplate,
			@Value("${wave2.content-service.base-url}") String baseUrl,
			@Qualifier("img") ImageFilePath imageUtils,
			StaticContentProxy staticContentProxy) {
		this.restClient = new StranglerRestClient(wave2RestTemplate);
		this.wave2RestTemplate = wave2RestTemplate;
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
		this.imageUtils = imageUtils;
		this.staticContentProxy = staticContentProxy;
	}

	@Override
	public ContentFolder getContentFolder(String folder, MerchantStore store) throws Exception {
		Validate.notNull(store, "MerchantStore cannot be null");
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/content/images");
		if (StringUtils.isNotBlank(folder)) {
			builder.queryParam("path", folder);
		}
		appendStoreLang(builder, store, null);
		return restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				ContentFolder.class,
				false);
	}

	@Override
	public String absolutePath(MerchantStore store, String file) {
		return new StringBuilder().append(imageUtils.getContextPath())
				.append(imageUtils.buildStaticImageUtils(store, file)).toString();
	}

	@Override
	public void delete(MerchantStore store, String fileName, String fileType) {
		Validate.notNull(store, "MerchantStore cannot be null");
		Validate.notNull(fileName, "File name cannot be null");
		ContentName name = new ContentName();
		name.setName(fileName);
		name.setContentType(fileType);
		restClient.exchangeVoid(
				contentUrl("/private/content/", store, null),
				HttpMethod.DELETE,
				name,
				true);
	}

	@Override
	public void delete(MerchantStore store, Long id) {
		Validate.notNull(store, "MerchantStore cannot be null");
		Validate.notNull(id, "Content id must not be null");
		restClient.exchangeVoid(
				contentUrl("/private/content/" + id, store, null),
				HttpMethod.DELETE,
				null,
				true);
	}

	@Override
	public ReadableEntityList<ReadableContentPage> getContentPages(MerchantStore store, Language language, int page,
			int count) {
		Validate.notNull(store, "MerchantStore cannot be null");
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/content/pages")
				.queryParam("page", page)
				.queryParam("count", count);
		appendStoreLang(builder, store, language);
		return restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<ReadableEntityList<ReadableContentPage>>() {},
				false);
	}

	@Override
	public ReadableContentPage getContentPage(String code, MerchantStore store, Language language) {
		Validate.notNull(code, "Content code cannot be null");
		Validate.notNull(store, "MerchantStore cannot be null");
		return restClient.exchange(
				contentUrl("/content/pages/" + code, store, language),
				HttpMethod.GET,
				null,
				ReadableContentPage.class,
				false);
	}

	@Override
	public ReadableContentPage getContentPageByName(String name, MerchantStore store, Language language) {
		Validate.notNull(name, "Content name cannot be null");
		Validate.notNull(store, "MerchantStore cannot be null");
		Validate.notNull(language, "Language cannot be null");
		return restClient.exchange(
				contentUrl("/content/pages/name/" + name, store, language),
				HttpMethod.GET,
				null,
				ReadableContentPage.class,
				false);
	}

	@Override
	public ReadableContentBox getContentBox(String code, MerchantStore store, Language language) {
		Validate.notNull(code, "Content code cannot be null");
		Validate.notNull(store, "MerchantStore cannot be null");
		return restClient.exchange(
				contentUrl("/content/boxes/" + code, store, language),
				HttpMethod.GET,
				null,
				ReadableContentBox.class,
				false);
	}

	@Override
	public boolean codeExist(String code, String type, MerchantStore store) {
		Validate.notNull(code, "Content code cannot be null");
		Validate.notNull(type, "Content type cannot be null");
		String path = BOX.equals(type)
				? "/private/content/box/" + code + "/exists"
				: "/private/content/page/" + code + "/exists";
		EntityExists exists = restClient.exchange(
				contentUrl(path, store, null),
				HttpMethod.GET,
				null,
				EntityExists.class,
				true);
		return exists != null && exists.isExists();
	}

	@Override
	public ReadableEntityList<ReadableContentBox> getContentBoxes(ContentType type, String codePrefix,
			MerchantStore store, Language language, int start, int count) {
		Validate.notNull(codePrefix, "content code prefix cannot be null");
		ReadableEntityList<ReadableContentBox> boxes = getContentBoxes(type, store, language, start, count);
		if (boxes == null || boxes.getItems() == null) {
			return boxes;
		}
		// ponytail: prefix filter is in-memory on one fetched page; no server-side prefix API
		List<ReadableContentBox> filtered = boxes.getItems().stream()
				.filter(box -> box.getCode() != null && box.getCode().startsWith(codePrefix))
				.collect(Collectors.toList());
		ReadableEntityList<ReadableContentBox> result = new ReadableEntityList<>();
		result.setItems(filtered);
		result.setNumber(filtered.size());
		result.setTotalPages(boxes.getTotalPages());
		result.setRecordsTotal(filtered.size());
		return result;
	}

	@Override
	public ReadableEntityList<ReadableContentBox> getContentBoxes(ContentType type, MerchantStore store,
			Language language, int page, int count) {
		Validate.notNull(store, "MerchantStore cannot be null");
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/content/boxes")
				.queryParam("page", page)
				.queryParam("count", count);
		appendStoreLang(builder, store, language);
		return restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<ReadableEntityList<ReadableContentBox>>() {},
				false);
	}

	@Override
	public void addContentFile(ContentFile file, String merchantStoreCode) {
		Validate.notNull(file, "ContentFile cannot be null");
		Validate.notNull(merchantStoreCode, "merchantStoreCode cannot be null");
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", fileResource(file));
		multipartExchange("/private/file", merchantStoreCode, null, body);
	}

	@Override
	public void addContentFiles(List<ContentFile> files, String merchantStoreCode) {
		Validate.notNull(files, "files cannot be null");
		for (ContentFile file : files) {
			addContentFile(file, merchantStoreCode);
		}
	}

	@Override
	public Long saveContentPage(PersistableContentPage page, MerchantStore merchantStore, Language language) {
		Validate.notNull(page, "page cannot be null");
		Validate.notNull(merchantStore, "MerchantStore cannot be null");
		Entity created = restClient.exchange(
				contentUrl("/private/content/page", merchantStore, language),
				HttpMethod.POST,
				page,
				Entity.class,
				true);
		return created != null ? created.getId() : null;
	}

	@Override
	public void updateContentPage(Long id, PersistableContentPage page, MerchantStore merchantStore,
			Language language) {
		Validate.notNull(id, "Content id must not be null");
		Validate.notNull(page, "page cannot be null");
		Validate.notNull(merchantStore, "MerchantStore cannot be null");
		page.setId(id);
		restClient.exchangeVoid(
				contentUrl("/private/content/page/" + id, merchantStore, language),
				HttpMethod.PUT,
				page,
				true);
	}

	@Override
	public void deleteContent(Long id, MerchantStore merchantStore) {
		Validate.notNull(id, "Content id must not be null");
		Validate.notNull(merchantStore, "MerchantStore cannot be null");
		restClient.exchangeVoid(
				contentUrl("/private/content/" + id, merchantStore, null),
				HttpMethod.DELETE,
				null,
				true);
	}

	@Override
	public Long saveContentBox(PersistableContentBox box, MerchantStore merchantStore, Language language) {
		Validate.notNull(box, "box cannot be null");
		Validate.notNull(merchantStore, "MerchantStore cannot be null");
		Entity created = restClient.exchange(
				contentUrl("/private/content/box", merchantStore, language),
				HttpMethod.POST,
				box,
				Entity.class,
				true);
		return created != null ? created.getId() : null;
	}

	@Override
	public void updateContentBox(Long id, PersistableContentBox box, MerchantStore merchantStore, Language language) {
		Validate.notNull(id, "Content id must not be null");
		Validate.notNull(box, "box cannot be null");
		Validate.notNull(merchantStore, "MerchantStore cannot be null");
		box.setId(id);
		restClient.exchangeVoid(
				contentUrl("/private/content/box/" + id, merchantStore, language),
				HttpMethod.PUT,
				box,
				true);
	}

	@Override
	public ReadableContentFull getContent(String code, MerchantStore store, Language language) {
		Validate.notNull(store, "MerchantStore not null");
		Validate.notNull(code, "Content code must not be null");
		return restClient.exchange(
				contentUrl("/private/content/any/" + code, store, language),
				HttpMethod.GET,
				null,
				ReadableContentFull.class,
				true);
	}

	@Override
	public List<ReadableContentEntity> getContents(Optional<String> type, MerchantStore store, Language language) {
		List<ReadableContentEntity> contents = restClient.exchange(
				contentUrl("/private/contents/any", store, language),
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<ReadableContentEntity>>() {},
				true);
		return contents != null ? contents : Collections.emptyList();
	}

	@Override
	public void renameFile(MerchantStore store, FileContentType fileType, String originalName, String newName) {
		Validate.notNull(store, "MerchantStore cannot be null");
		Validate.notNull(fileType, "FileContentType cannot be null");
		Validate.notNull(originalName, "originalName cannot be null");
		Validate.notNull(newName, "newName cannot be null");
		String path = "/files/" + store.getCode() + "/" + fileType.name() + "/" + originalName;
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/private/content/images/rename")
				.queryParam("path", path)
				.queryParam("newName", newName);
		appendStoreLang(builder, store, null);
		restClient.exchangeVoid(builder.toUriString(), HttpMethod.POST, null, true);
	}

	@Override
	public OutputContentFile download(MerchantStore store, FileContentType fileType, String fileName) {
		Validate.notNull(store, "MerchantStore cannot be null");
		Validate.notNull(fileType, "FileContentType cannot be null");
		Validate.notNull(fileName, "fileName cannot be null");
		if (fileType != FileContentType.IMAGE) {
			throw new UnsupportedOperationException("download only supported for IMAGE via static internal API");
		}
		byte[] bytes = staticContentProxy.getStaticFile(store.getCode(), fileType, fileName);
		OutputContentFile output = new OutputContentFile();
		output.setFileContentType(fileType);
		output.setFileName(fileName);
		ByteArrayOutputStream stream = new ByteArrayOutputStream(bytes.length);
		try {
			stream.write(bytes);
		} catch (IOException e) {
			throw new IllegalStateException("Cannot write downloaded content", e);
		}
		output.setFile(stream);
		return output;
	}

	private String contentUrl(String path, MerchantStore store, Language language) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1" + path);
		appendStoreLang(builder, store, language);
		return builder.toUriString();
	}

	private void appendStoreLang(UriComponentsBuilder builder, MerchantStore store, Language language) {
		if (store != null && StringUtils.isNotBlank(store.getCode())) {
			builder.queryParam("store", store.getCode());
		}
		if (language != null && StringUtils.isNotBlank(language.getCode())) {
			builder.queryParam("lang", language.getCode());
		}
	}

	private void appendStore(UriComponentsBuilder builder, String storeCode) {
		if (StringUtils.isNotBlank(storeCode)) {
			builder.queryParam("store", storeCode);
		}
	}

	private void multipartExchange(String path, String storeCode, Language language, MultiValueMap<String, Object> body) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1" + path);
		appendStore(builder, storeCode);
		if (language != null && StringUtils.isNotBlank(language.getCode())) {
			builder.queryParam("lang", language.getCode());
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		forwardAuthorization(headers);
		wave2RestTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST,
				new HttpEntity<>(body, headers),
				Void.class);
	}

	private static ByteArrayResource fileResource(ContentFile file) {
		return new ByteArrayResource(file.getFile()) {
			@Override
			public String getFilename() {
				return file.getName();
			}
		};
	}

	private static void forwardAuthorization(HttpHeaders headers) {
		String authorization = currentHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.isNotBlank(authorization)) {
			headers.set(HttpHeaders.AUTHORIZATION, authorization);
		}
	}

	private static String currentHeader(String name) {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (!(attrs instanceof ServletRequestAttributes)) {
			return null;
		}
		HttpServletRequest request = ((ServletRequestAttributes) attrs).getRequest();
		return request != null ? request.getHeader(name) : null;
	}
}

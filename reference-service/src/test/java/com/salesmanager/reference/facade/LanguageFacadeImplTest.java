package com.salesmanager.reference.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.reference.support.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class LanguageFacadeImplTest {

	@Mock
	private LanguageService languageService;

	@InjectMocks
	private LanguageFacadeImpl languageFacade;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void getLanguages_returnsReadableLanguageWithIdCodeSortOrderOnly() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		language.setSortOrder(10);
		when(languageService.getLanguages()).thenReturn(Collections.singletonList(language));

		List<ReadableLanguage> result = languageFacade.getLanguages();

		assertEquals(1, result.size());
		ReadableLanguage dto = result.get(0);
		assertEquals(1, dto.getId());
		assertEquals("en", dto.getCode());
		assertEquals(10, dto.getSortOrder());

		JsonNode json = objectMapper.valueToTree(dto);
		assertEquals(3, json.size());
		json.fieldNames().forEachRemaining(name -> {
			if (!name.equals("id") && !name.equals("code") && !name.equals("sortOrder")) {
				throw new AssertionError("Unexpected field: " + name);
			}
		});
	}

	@Test
	void getLanguages_empty_throwsNotFound() throws Exception {
		when(languageService.getLanguages()).thenReturn(Collections.emptyList());
		assertThrows(ResourceNotFoundException.class, () -> languageFacade.getLanguages());
	}

	@Test
	void getLanguages_serviceFailure_wraps() throws Exception {
		when(languageService.getLanguages()).thenThrow(new ServiceException("db"));
		assertThrows(RuntimeException.class, () -> languageFacade.getLanguages());
	}
}

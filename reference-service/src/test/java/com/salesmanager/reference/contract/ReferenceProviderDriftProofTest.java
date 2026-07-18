package com.salesmanager.reference.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import au.com.dius.pact.core.matchers.BodyMatchResult;
import au.com.dius.pact.core.matchers.BodyMismatch;
import au.com.dius.pact.core.matchers.JsonContentMatcher;
import au.com.dius.pact.core.matchers.MatchingContext;
import au.com.dius.pact.core.matchers.Mismatch;
import au.com.dius.pact.core.model.ContentType;
import au.com.dius.pact.core.model.OptionalBody;
import au.com.dius.pact.core.model.matchingrules.MatchingRuleCategory;
import au.com.dius.pact.core.model.matchingrules.MatchingRulesImpl;
import au.com.dius.pact.core.model.matchingrules.TypeMatcher;

/**
 * Controlled proof that omitting a consumer-required field fails Pact body matching (drift gate).
 */
class ReferenceProviderDriftProofTest {

	@Test
	void missingRequiredLanguageCode_failsBodyMatch() {
		// Consumer expects ReadableLanguage wire fields: id, code, sortOrder (ADR-005).
		byte[] expected = "[{\"id\":1,\"code\":\"en\",\"sortOrder\":0}]".getBytes(StandardCharsets.UTF_8);
		// Provider drift: required "code" removed.
		byte[] actual = "[{\"id\":1,\"sortOrder\":0}]".getBytes(StandardCharsets.UTF_8);

		MatchingRulesImpl rules = new MatchingRulesImpl();
		MatchingRuleCategory body = rules.addCategory("body");
		body.addRule("$[0].id", TypeMatcher.INSTANCE);
		body.addRule("$[0].code", TypeMatcher.INSTANCE);
		body.addRule("$[0].sortOrder", TypeMatcher.INSTANCE);

		MatchingContext context = new MatchingContext(body, true);
		BodyMatchResult result = JsonContentMatcher.INSTANCE.matchBody(
				OptionalBody.body(expected, ContentType.getJSON()),
				OptionalBody.body(actual, ContentType.getJSON()),
				context);

		assertThat(result.matchedOk())
				.as("removing required field code must fail consumer contract match")
				.isFalse();

		List<String> paths = result.getMismatches().stream()
				.filter(BodyMismatch.class::isInstance)
				.map(m -> ((BodyMismatch) m).getPath())
				.collect(Collectors.toList());
		assertThat(paths.stream().anyMatch(p -> p != null && p.contains("code"))
				|| result.getMismatches().stream().map(Mismatch::description)
						.anyMatch(d -> d != null && d.toLowerCase().contains("code")))
				.as("mismatch should mention missing code")
				.isTrue();
	}
}

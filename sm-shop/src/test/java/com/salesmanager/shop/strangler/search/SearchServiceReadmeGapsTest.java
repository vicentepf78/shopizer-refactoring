package com.salesmanager.shop.strangler.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class SearchServiceReadmeGapsTest {

	@Test
	void readmeDocumentsGapSrch01Through10() throws IOException {
		Path readme = Paths.get("..", "search-service", "README.md").normalize().toAbsolutePath();
		assertThat(readme).exists();
		String content = Files.readString(readme, StandardCharsets.UTF_8);
		IntStream.rangeClosed(1, 10)
				.forEach(i -> assertThat(content).contains("GAP-SRCH-" + String.format("%02d", i)));
		assertThat(content).containsIgnoringCase("known gaps");
	}
}

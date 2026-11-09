package com.etude.platform.web

import com.etude.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.assertj.MockMvcTester
import tools.jackson.databind.json.JsonMapper
import java.nio.file.Files
import java.nio.file.Path

/**
 * Keeps the committed OpenAPI documents in `openapi/` equal to what the code generates.
 * On a difference the file is rewritten and the test fails: review the diff and commit it.
 */
@IntegrationTest
class OpenApiSpecTest(
    @Autowired private val mvc: MockMvcTester,
) {

    private val mapper = JsonMapper.builder().build()

    @ParameterizedTest
    @ValueSource(strings = ["customer", "admin"])
    fun `committed OpenAPI document matches the code`(group: String) {
        val generated = mvc.get().uri("/v3/api-docs/{group}", group).exchange().response.contentAsString
        // Line endings are normalized to LF so the check behaves the same on Windows and Linux.
        val pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(generated))
            .replace("\r\n", "\n") + "\n"
        val file = Path.of("openapi", "$group.json")
        val committed = if (Files.exists(file)) Files.readString(file).replace("\r\n", "\n") else ""

        if (committed != pretty) {
            Files.createDirectories(file.parent)
            Files.writeString(file, pretty)
        }
        assertThat(committed)
            .withFailMessage("OpenAPI document $file was out of date and has been regenerated. Review and commit it.")
            .isEqualTo(pretty)
    }
}

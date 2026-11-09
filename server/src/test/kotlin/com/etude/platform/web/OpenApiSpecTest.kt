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
 * 커밋한 OpenAPI 문서가 코드와 같은지 확인한다. 다르면 파일을 새로 쓰고 실패한다.
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
        // OS와 관계없이 비교하도록 줄바꿈을 LF로 맞춘다.
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

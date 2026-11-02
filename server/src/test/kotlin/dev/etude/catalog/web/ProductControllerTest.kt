package dev.etude.catalog.web

import dev.etude.TestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.util.UUID

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest(
    @Autowired private val mvc: MockMvcTester,
) {

    @Test
    fun `registers a product and reads it back`() {
        val created = mvc.post().uri("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Keyboard","priceAmount":10000.4,"priceCurrency":"KRW"}""")
            .exchange()

        assertThat(created).hasStatus(HttpStatus.CREATED)
        val location = requireNotNull(created.response.getHeader("Location"))

        assertThat(mvc.get().uri(location).exchange())
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(
                """{"name":"Keyboard","priceAmount":10000,"priceCurrency":"KRW","salesStatus":"ON_SALE"}""",
            )
    }

    @Test
    fun `returns 404 for an unknown product`() {
        assertThat(mvc.get().uri("/api/products/{id}", UUID.randomUUID()).exchange())
            .hasStatus(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `rejects a blank name`() {
        val result = mvc.post().uri("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":" ","priceAmount":1000,"priceCurrency":"KRW"}""")
            .exchange()

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST)
    }
}

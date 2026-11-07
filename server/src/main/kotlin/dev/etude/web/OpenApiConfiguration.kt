package dev.etude.web

import dev.etude.shared.Money
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI documents generated from the code: `/v3/api-docs/customer` and `/v3/api-docs/admin`, browsable at
 * `/swagger-ui.html`. The committed copies under `server/openapi/` are checked by `OpenApiSpecTest`.
 */
@Configuration(proxyBeanMethods = false)
class OpenApiConfiguration {

    init {
        // Money is written by MoneyJson, not by its Kotlin properties; every use refers to one Money schema.
        SpringDocUtils.getConfig().replaceWithSchema(Money::class.java, Schema<Any>().`$ref`(MONEY))
    }

    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(Info().title("Etude API").version("v1").description(CONVENTIONS))
        .servers(listOf(Server().url("/")))

    /** Adds the shared schemas and documents the error body as the `default` response of every operation. */
    @Bean
    fun problemResponses(): GlobalOpenApiCustomizer = GlobalOpenApiCustomizer { openApi ->
        openApi.components.addSchemas(MONEY, moneySchema()).addSchemas(PROBLEM, problemSchema())
        val response = ApiResponse()
            .description("Error (see `code`)")
            .content(Content().addMediaType("application/problem+json", MediaType().schema(Schema<Any>().`$ref`(PROBLEM))))
        openApi.paths.values.flatMap { it.readOperations() }.forEach { it.responses.addApiResponse("default", response) }
    }

    @Bean
    fun customerApi(): GroupedOpenApi = GroupedOpenApi.builder().group("customer").pathsToMatch("/api/v1/**").build()

    @Bean
    fun adminApi(): GroupedOpenApi = GroupedOpenApi.builder().group("admin").pathsToMatch("/admin/v1/**").build()

    private fun moneySchema(): Schema<Any> = ObjectSchema()
        .addProperty("amount", StringSchema().example("12.50").description("Decimal string in the currency's minor units"))
        .addProperty("currency", StringSchema().example("USD").description("ISO 4217 currency code"))
        .apply { required = listOf("amount", "currency") }

    private fun problemSchema(): Schema<Any> = ObjectSchema()
        .addProperty("type", StringSchema())
        .addProperty("title", StringSchema())
        .addProperty("status", IntegerSchema())
        .addProperty("detail", StringSchema())
        .addProperty("instance", StringSchema())
        .addProperty("code", StringSchema().example("INVALID_REQUEST"))
        .addProperty(
            "errors",
            ArraySchema().items(
                ObjectSchema()
                    .addProperty("field", StringSchema().example("lines[0].quantity"))
                    .addProperty("code", StringSchema().example("Positive"))
                    .addProperty("message", StringSchema()),
            ),
        )
        .apply { required = listOf("code", "status", "title") }

    private companion object {
        const val MONEY = "Money"
        const val PROBLEM = "Problem"
        val CONVENTIONS = """
            ## Conventions

            - **Instants:** ISO-8601 UTC strings ending in `Z`, microsecond precision. Clients convert to the user's time zone.
            - **Money:** `{"amount": "12.50", "currency": "USD"}`. `amount` is a decimal string in the currency's minor units;
              do not do arithmetic on it as a floating-point number. Requests accept a string or a number and reject unknown
              currencies and amounts with more decimal places than the currency allows.
            - **Errors:** `application/problem+json` with a `code` property. Branch on `code`; `detail` is English text for developers.

            | `code` | Status | When |
            |---|---|---|
            | `INVALID_REQUEST` | 400 | Validation failure, unreadable body, malformed path variable. `errors[]` lists `{field, code, message}` |
            | `NOT_FOUND` | 404 | Unknown resource or path |
            | `INTERNAL_ERROR` | 500 | Unexpected failure |
            | other | 4xx | HTTP status name (e.g. `METHOD_NOT_ALLOWED`) or a business code of the endpoint |

            `errors[].code` is the violated constraint (`NotBlank`, `Positive`, ...) or `InvalidValue` when the body could not be read.
        """.trimIndent()
    }
}

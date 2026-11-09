package com.etude.platform.web

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.SpecVersion
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springdoc.core.utils.SpringDocAnnotationsUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

/**
 * 코드에서 생성하는 OpenAPI 문서: `/v3/api-docs/customer`, `/v3/api-docs/admin`. local 프로필에서는
 * `/swagger-ui.html`로 볼 수 있다. `server/openapi/`에 커밋한 사본은 `OpenApiSpecTest`가 검사하고,
 * 모든 통합 테스트 응답을 이 사본으로 검증한다.
 */
@Configuration(proxyBeanMethods = false)
class OpenApiConfiguration {

    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(Info().title("Etude API").version("v1").description(readDescription()))
        .servers(listOf(Server().url("/")))

    @Bean
    fun customerApi(): GroupedOpenApi = GroupedOpenApi.builder().group("customer").pathsToMatch("/api/v1/**").build()

    @Bean
    fun adminApi(): GroupedOpenApi = GroupedOpenApi.builder().group("admin").pathsToMatch("/admin/v1/**").build()

    /** 모든 API는 공통 오류 본문으로 실패할 수 있다. */
    @Bean
    fun defaultErrorResponse(): GlobalOpenApiCustomizer = GlobalOpenApiCustomizer { openApi ->
        val problem = SpringDocAnnotationsUtils.extractSchema(openApi.components, ProblemBody::class.java, null, null, SpecVersion.V31)
        val response = ApiResponse()
            .description("Error. Branch on `code`.")
            .content(Content().addMediaType(PROBLEM_JSON, MediaType().schema(problem)))
        openApi.paths.values.flatMap { it.readOperations() }.forEach { it.responses.addApiResponse("default", response) }
    }

    private fun readDescription(): String = ClassPathResource("openapi-description.md").inputStream.use {
        it.readAllBytes().toString(Charsets.UTF_8).trimEnd()
    }

    companion object {
        const val PROBLEM_JSON = "application/problem+json"
    }
}

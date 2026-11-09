package com.etude.platform.web

import com.etude.platform.Rejects
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springdoc.core.customizers.GlobalOperationCustomizer
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod

/**
 * 컨트롤러 시그니처에 드러나지 않는 것을 문서에 더한다. 201 응답의 `Location` 헤더, 그리고 [Rejects]를 붙인 메서드의
 * 업무 오류 코드를 모듈의 [com.etude.platform.RejectionStatuses]가 정한 상태별로 묶어 넣는다.
 */
@Component
class ApiOperationCustomizer(
    private val rejectionStatuses: RejectionStatusRegistry,
) : GlobalOperationCustomizer {

    override fun customize(operation: Operation, handlerMethod: HandlerMethod): Operation {
        operation.responses?.get("201")?.addHeaderObject(
            "Location",
            Header().description("URI of the created resource").schema(StringSchema().format("uri-reference")),
        )
        handlerMethod.getMethodAnnotation(Rejects::class.java)?.let { addRejections(operation, it) }
        return operation
    }

    private fun addRejections(operation: Operation, rejects: Rejects) {
        val reasonType = rejects.reasons.java
        val statuses = rejectionStatuses.forType(reasonType)
        reasonType.enumConstants
            .groupBy { statuses.statusOfAny(it) }
            .forEach { (status, reasons) ->
                // 400은 프레임워크의 INVALID_REQUEST일 수도 있어서 문서의 코드 목록에 함께 넣는다.
                val codes = reasons.map { it.name } + listOfNotNull(GlobalExceptionHandler.INVALID_REQUEST.takeIf { status.value() == 400 })
                val schema = Schema<Any>().allOf(
                    listOf(
                        Schema<Any>().`$ref`("Problem"),
                        ObjectSchema().addProperty("code", StringSchema()._enum(codes)),
                    ),
                )
                operation.responses.addApiResponse(
                    status.value().toString(),
                    ApiResponse()
                        .description("Error codes: ${codes.joinToString(", ")}")
                        .content(Content().addMediaType(OpenApiConfiguration.PROBLEM_JSON, MediaType().schema(schema))),
                )
            }
    }
}

package dev.etude.platform.web

import dev.etude.platform.RejectionStatuses
import dev.etude.platform.Rejects
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
 * Adds what the controller signatures do not show: the `Location` header of 201 responses, and the business error
 * codes of methods annotated with [Rejects], grouped by the status from the module's [RejectionStatuses].
 */
@Component
class ApiOperationCustomizer(
    private val rejectionStatuses: List<RejectionStatuses<*>>,
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
        val statuses = rejectionStatuses.find { it.reasonType == reasonType }
            ?: error("No RejectionStatuses bean for ${reasonType.name}")
        reasonType.enumConstants
            .groupBy { statuses.statusOfAny(it) }
            .forEach { (status, reasons) ->
                // A 400 can also be the framework's INVALID_REQUEST, so its documented codes include it.
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

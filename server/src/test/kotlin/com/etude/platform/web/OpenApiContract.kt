package com.etude.platform.web

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.networknt.schema.Schema
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.SpecificationVersion
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultHandler
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * 통합 테스트의 모든 응답을 커밋한 OpenAPI 문서로 검증한다. 문서에 없는 경로는 건너뛴다.
 */
class OpenApiContract(documents: List<Path>) : ResultHandler {

    private val mapper = ObjectMapper()
    private val registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
    private val operations = documents.flatMap { readOperations(mapper.readTree(Files.readString(it))) }
    private val schemas = ConcurrentHashMap<String, Schema>()

    override fun handle(result: MvcResult) {
        val request = result.request
        val response = result.response
        val uri = request.requestURI ?: return
        val operation = operations.find { it.method == request.method && it.path.matches(uri) } ?: return
        val where = "${request.method} ${request.requestURI} -> ${response.status}"

        val documented = operation.responses[response.status.toString()] ?: operation.responses["default"]
            ?: throw AssertionError("$where: status not documented")
        val content = documented["content"] ?: return
        val body = response.contentAsString
        if (body.isBlank()) throw AssertionError("$where: documented body is missing")

        val actualType = MediaType.parseMediaType(requireNotNull(response.contentType) { "$where: no Content-Type" })
        val mediaType = content.fieldNames().asSequence().find { MediaType.parseMediaType(it).isCompatibleWith(actualType) }
            ?: throw AssertionError("$where: content type $actualType not documented")

        val schema = schemas.getOrPut("${operation.key} ${response.status} $mediaType") {
            registry.getSchema(standalone(content[mediaType]["schema"], operation.components))
        }
        val errors = schema.validate(mapper.readTree(body))
        if (errors.isNotEmpty()) {
            throw AssertionError("$where: body does not match the OpenAPI document\n  ${errors.joinToString("\n  ")}\n  body: $body")
        }
    }

    /** [schema]에 문서의 component 스키마를 `$defs`로 붙인 JSON Schema 문서. */
    private fun standalone(schema: JsonNode, components: JsonNode): JsonNode {
        val root = (rewriteRefs(schema.deepCopy()) as ObjectNode)
        root.put("\$schema", "https://json-schema.org/draft/2020-12/schema")
        root.set<JsonNode>("\$defs", rewriteRefs(components.deepCopy()))
        return root
    }

    private fun rewriteRefs(node: JsonNode): JsonNode {
        if (node is ObjectNode) {
            node.properties().forEach { (name, value) ->
                if (name == "\$ref" && value.isTextual) {
                    node.set<JsonNode>(name, TextNode(value.asText().replace("#/components/schemas/", "#/\$defs/")))
                } else {
                    rewriteRefs(value)
                }
            }
        } else if (node.isArray) {
            node.forEach(::rewriteRefs)
        }
        return node
    }

    private fun readOperations(document: JsonNode): List<DocumentedOperation> {
        val components = document.path("components").path("schemas")
        return document["paths"].properties().flatMap { (path, item) ->
            val pattern = Regex("^" + path.replace(Regex("""\{[^/]+}"""), "[^/]+") + "$")
            item.properties().map { (method, operation) ->
                DocumentedOperation(
                    key = "$method $path",
                    method = method.uppercase(),
                    path = pattern,
                    responses = operation["responses"].properties().associate { it.key to it.value },
                    components = components,
                )
            }
        }
    }

    private class DocumentedOperation(
        val key: String,
        val method: String,
        val path: Regex,
        val responses: Map<String, JsonNode>,
        val components: JsonNode,
    )
}

@TestConfiguration(proxyBeanMethods = false)
class OpenApiContractConfiguration {

    @Bean
    fun openApiContract(): MockMvcBuilderCustomizer = MockMvcBuilderCustomizer { builder ->
        builder.alwaysDo(OpenApiContract(listOf(Path.of("openapi", "customer.json"), Path.of("openapi", "admin.json"))))
    }
}

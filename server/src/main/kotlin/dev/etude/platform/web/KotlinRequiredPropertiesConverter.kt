package dev.etude.platform.web

import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverter
import io.swagger.v3.core.converter.ModelConverterContext
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.media.Schema
import org.springframework.stereotype.Component
import kotlin.reflect.full.memberProperties

/**
 * Marks non-nullable Kotlin properties as `required` in the OpenAPI schema, so generated clients get
 * non-optional fields. Swagger only reads requiredness from annotations, not from Kotlin types.
 */
@Component
class KotlinRequiredPropertiesConverter : ModelConverter {

    override fun resolve(type: AnnotatedType, context: ModelConverterContext, chain: Iterator<ModelConverter>): Schema<*>? {
        val schema = if (chain.hasNext()) chain.next().resolve(type, context, chain) else null
        val javaClass = Json.mapper().constructType(type.type).rawClass
        if (schema == null || !javaClass.isAnnotationPresent(Metadata::class.java)) return schema

        val model = schema.`$ref`?.let { context.definedModels[it.substringAfterLast('/')] } ?: schema
        val properties = model.properties ?: return schema
        val nonNull = javaClass.kotlin.memberProperties
            .filter { !it.returnType.isMarkedNullable && it.name in properties }
            .map { it.name }
        model.required = ((model.required ?: emptyList()) + nonNull).distinct().sorted()
        return schema
    }
}

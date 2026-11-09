package com.etude.platform.web

import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverter
import io.swagger.v3.core.converter.ModelConverterContext
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.media.Schema
import org.springframework.stereotype.Component
import kotlin.reflect.full.memberProperties

/**
 * null이 될 수 없는 Kotlin 속성을 OpenAPI 스키마에서 `required`로 표시해, 생성한 클라이언트 타입도 필수 필드가 되게 한다.
 * Swagger는 필수 여부를 애노테이션에서만 읽고 Kotlin 타입은 보지 않는다.
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

package com.etude.platform.web

import com.etude.shared.Money
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverter
import io.swagger.v3.core.converter.ModelConverterContext
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.media.Schema
import org.springframework.stereotype.Component

/** [Money]를 [MoneyJson]이 실제로 쓰는 타입인 [MoneyJson.Body]로 문서화한다. */
@Component
class MoneySchemaConverter : ModelConverter {

    override fun resolve(type: AnnotatedType, context: ModelConverterContext, chain: Iterator<ModelConverter>): Schema<*>? {
        if (Json.mapper().constructType(type.type).rawClass == Money::class.java) {
            return context.resolve(AnnotatedType(MoneyJson.Body::class.java).resolveAsRef(true))
        }
        return if (chain.hasNext()) chain.next().resolve(type, context, chain) else null
    }
}

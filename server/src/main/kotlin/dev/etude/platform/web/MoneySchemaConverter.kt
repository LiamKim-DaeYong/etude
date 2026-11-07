package dev.etude.platform.web

import dev.etude.shared.Money
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverter
import io.swagger.v3.core.converter.ModelConverterContext
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.media.Schema
import org.springframework.stereotype.Component

/** Documents [Money] with the type that [MoneyJson] actually writes, [MoneyJson.Body]. */
@Component
class MoneySchemaConverter : ModelConverter {

    override fun resolve(type: AnnotatedType, context: ModelConverterContext, chain: Iterator<ModelConverter>): Schema<*>? {
        if (Json.mapper().constructType(type.type).rawClass == Money::class.java) {
            return context.resolve(AnnotatedType(MoneyJson.Body::class.java).resolveAsRef(true))
        }
        return if (chain.hasNext()) chain.next().resolve(type, context, chain) else null
    }
}

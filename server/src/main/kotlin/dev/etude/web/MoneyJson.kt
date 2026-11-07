package dev.etude.web

import dev.etude.shared.Money
import org.springframework.boot.jackson.JacksonComponent
import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.ValueSerializer
import java.math.BigDecimal
import java.util.Currency

/**
 * JSON form of [Money] for the whole API: `{"amount": "12.50", "currency": "USD"}`.
 *
 * The amount is a decimal string so clients do not lose precision to floating point. Reading accepts a string or
 * a number, and rejects unknown currencies and amounts with more decimal places than the currency allows.
 */
@JacksonComponent
class MoneyJson {

    class Serializer : ValueSerializer<Money>() {
        override fun serialize(value: Money, gen: JsonGenerator, ctxt: SerializationContext) {
            gen.writeStartObject()
            gen.writeStringProperty("amount", value.amount.toPlainString())
            gen.writeStringProperty("currency", value.currency.currencyCode)
            gen.writeEndObject()
        }
    }

    class Deserializer : ValueDeserializer<Money>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Money {
            val node = ctxt.readTree(p)
            val amount = node.get("amount")
            val currency = node.get("currency")
            if (amount == null || amount.isNull || currency == null || !currency.isString) {
                return ctxt.reportInputMismatch(this, "Money requires 'amount' and 'currency'")
            }
            val parsedAmount = parseAmount(amount)
                ?: return ctxt.reportInputMismatch(this, "Invalid amount: ${amount.asString()}")
            val parsedCurrency = runCatching { Currency.getInstance(currency.asString()) }.getOrNull()
                ?: return ctxt.reportInputMismatch(this, "Unsupported currency: ${currency.asString()}")
            return try {
                Money.ofExact(parsedAmount, parsedCurrency)
            } catch (e: IllegalArgumentException) {
                ctxt.reportInputMismatch(this, e.message ?: "Invalid money")
            }
        }

        private fun parseAmount(node: JsonNode): BigDecimal? = when {
            node.isString -> node.asString().toBigDecimalOrNull()
            node.isNumber -> node.decimalValue()
            else -> null
        }
    }
}

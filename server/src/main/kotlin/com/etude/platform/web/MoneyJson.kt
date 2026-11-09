package com.etude.platform.web

import com.etude.shared.Money
import io.swagger.v3.oas.annotations.media.Schema
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
 * API 전체에서 쓰는 [Money]의 JSON 형식: `{"amount": "12.50", "currency": "USD"}`.
 *
 * 금액은 10진 문자열이라 클라이언트가 부동소수점으로 정밀도를 잃지 않는다. 읽을 때는 문자열과 숫자를 모두 받고,
 * 모르는 통화와 통화가 허용하는 것보다 소수 자릿수가 많은 금액은 거절한다.
 */
@JacksonComponent
class MoneyJson {

    /** [Money]를 쓸 때 만드는 JSON 객체. OpenAPI 스키마도 이 클래스에서 만든다. */
    @Schema(name = "Money")
    data class Body(
        @field:Schema(description = "Decimal string in the currency's minor units", example = "12.50")
        val amount: String,
        @field:Schema(description = "ISO 4217 currency code", example = "USD")
        val currency: String,
    )

    class Serializer : ValueSerializer<Money>() {
        override fun serialize(value: Money, gen: JsonGenerator, ctxt: SerializationContext) {
            ctxt.writeValue(gen, Body(value.amount.toPlainString(), value.currency.currencyCode))
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

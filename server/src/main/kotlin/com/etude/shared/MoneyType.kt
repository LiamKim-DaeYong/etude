package com.etude.shared

import org.hibernate.metamodel.spi.ValueAccess
import org.hibernate.usertype.CompositeUserType
import java.io.Serializable
import java.math.BigDecimal
import java.util.Currency

/**
 * [Money]를 `<attribute>_amount`, `<attribute>_currency` 두 칼럼에 매핑한다. `@CompositeType(MoneyType::class)`로 쓴다.
 */
class MoneyType : CompositeUserType<Money> {

    /** 칼럼 구성. Hibernate는 속성을 알파벳 순으로 둔다: amount(0), currency(1). */
    class Columns {
        var amount: BigDecimal? = null
        var currency: Currency? = null
    }

    override fun embeddable(): Class<*> = Columns::class.java

    override fun returnedClass(): Class<Money> = Money::class.java

    override fun getPropertyValue(component: Money, property: Int): Any = when (property) {
        0 -> component.amount
        1 -> component.currency
        else -> throw IllegalArgumentException("Unknown Money property index: $property")
    }

    override fun instantiate(values: ValueAccess): Money = Money.of(
        values.getValue(0, BigDecimal::class.java),
        values.getValue(1, Currency::class.java),
    )

    override fun equals(x: Money?, y: Money?): Boolean = x == y

    override fun hashCode(x: Money): Int = x.hashCode()

    override fun deepCopy(value: Money?): Money? = value

    override fun isMutable(): Boolean = false

    override fun disassemble(value: Money): Serializable = arrayOf(value.amount, value.currency.currencyCode)

    override fun assemble(cached: Serializable, owner: Any?): Money {
        val (amount, currency) = cached as Array<*>
        return Money.of(amount as BigDecimal, Currency.getInstance(currency as String))
    }

    override fun replace(detached: Money?, managed: Money?, owner: Any?): Money? = detached
}

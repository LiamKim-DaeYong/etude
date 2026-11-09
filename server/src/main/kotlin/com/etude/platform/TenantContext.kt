package com.etude.platform

import java.util.UUID

/**
 * The tenant that the current work belongs to (ADR-0006). Bind it outside the transaction: Hibernate reads it when
 * the transaction opens its session, so a tenant bound inside a running transaction is not seen.
 */
object TenantContext {

    /** Hibernate's root tenant: platform work outside any tenant, such as sign-up. */
    val PLATFORM: UUID = UUID(0, 0)

    private val tenantId: ScopedValue<UUID> = ScopedValue.newInstance()

    fun currentTenantId(): UUID? = if (tenantId.isBound) tenantId.get() else null

    fun isPlatform(): Boolean = currentTenantId() == PLATFORM

    fun <T> runAs(tenantId: UUID, block: () -> T): T {
        require(tenantId != PLATFORM) { "Use runAsPlatform for work outside any tenant" }
        return bind(tenantId, block)
    }

    fun <T> runAsPlatform(block: () -> T): T = bind(PLATFORM, block)

    private fun <T> bind(value: UUID, block: () -> T): T = ScopedValue.where(tenantId, value).call<T, RuntimeException> { block() }
}

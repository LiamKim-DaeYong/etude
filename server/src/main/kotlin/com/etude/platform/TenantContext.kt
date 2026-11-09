package com.etude.platform

import java.util.UUID

/**
 * 지금 하는 작업이 속한 회사(ADR-0006). 트랜잭션 바깥에서 건다. Hibernate는 트랜잭션이 세션을 열 때 회사를 읽으므로,
 * 이미 시작한 트랜잭션 안에서 건 회사는 보지 못한다.
 */
object TenantContext {

    /** Hibernate의 root 테넌트. 회원가입처럼 어느 회사에도 속하지 않는 플랫폼 작업을 뜻한다. */
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

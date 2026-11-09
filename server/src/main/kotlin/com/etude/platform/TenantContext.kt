package com.etude.platform

import java.util.UUID

/**
 * 현재 작업의 회사(ADR-0006).
 *
 * 트랜잭션 바깥에서 설정한다. Hibernate는 트랜잭션 시작 시점의 회사를 쓴다.
 */
object TenantContext {

    /** 회사에 속하지 않는 플랫폼 작업(회원가입, 로그인). Hibernate의 root 테넌트다. */
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

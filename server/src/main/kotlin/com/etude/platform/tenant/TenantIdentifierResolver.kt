package com.etude.platform.tenant

import com.etude.platform.TenantContext
import org.hibernate.cfg.MultiTenancySettings
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Hibernate에 현재 회사를 알린다(ADR-0006 2.2, ADR-0008).
 *
 * 회사가 정해지지 않은 작업은 [NONE]으로 알린다. root(플랫폼 블록)도 실제 회사도 아니므로 `@TenantId` 조건이
 * 어떤 행에도 맞지 않는다. 서버 시작, 로그인 전 요청, 아직 회사로 나누지 않은 모듈은 Hibernate 세션을 그대로 쓰고,
 * 테넌트 데이터는 [TenantRepositoryGuard], [PlatformWriteGuard], RLS가 막는다.
 */
@Component
class TenantIdentifierResolver :
    CurrentTenantIdentifierResolver<UUID>,
    HibernatePropertiesCustomizer {

    override fun resolveCurrentTenantIdentifier(): UUID = TenantContext.currentTenantId() ?: NONE

    override fun validateExistingCurrentSessions(): Boolean = true

    override fun isRoot(tenantId: UUID?): Boolean = tenantId == TenantContext.PLATFORM

    override fun customize(hibernateProperties: MutableMap<String, Any>) {
        hibernateProperties[MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER] = this
    }

    companion object {
        /** 회사가 정해지지 않음. 회원가입으로 발급하는 UUIDv7과 겹치지 않는다. */
        val NONE: UUID = UUID(0, 1)

        /** 실제 회사로 실행 중인지. 미지정(값이 없거나 [NONE])과 플랫폼 블록은 아니다. */
        fun isTenantBound(): Boolean = isTenant(TenantContext.currentTenantId())

        fun isTenant(tenantId: UUID?): Boolean = tenantId != null && tenantId != TenantContext.PLATFORM && tenantId != NONE
    }
}

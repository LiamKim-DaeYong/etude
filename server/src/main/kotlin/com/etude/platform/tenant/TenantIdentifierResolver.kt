package com.etude.platform.tenant

import org.hibernate.cfg.MultiTenancySettings
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TenantIdentifierResolver :
    CurrentTenantIdentifierResolver<UUID>,
    HibernatePropertiesCustomizer {

    override fun resolveCurrentTenantIdentifier(): UUID? = TenantContext.currentTenantId()

    override fun validateExistingCurrentSessions(): Boolean = true

    override fun isRoot(tenantId: UUID?): Boolean = tenantId == TenantContext.PLATFORM

    override fun customize(hibernateProperties: MutableMap<String, Any>) {
        hibernateProperties[MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER] = this
    }
}

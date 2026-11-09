package com.etude.tenant.domain

import org.springframework.data.repository.Repository
import java.util.UUID

interface TenantRepository : Repository<Tenant, UUID> {

    fun save(tenant: Tenant): Tenant
}

package com.etude.platform

import java.util.UUID

/** A signed-in principal that belongs to a tenant. The security filter runs each request as its [tenantId]. */
interface TenantPrincipal {
    val tenantId: UUID
}

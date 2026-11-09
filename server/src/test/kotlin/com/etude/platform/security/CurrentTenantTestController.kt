package com.etude.platform.security

import com.etude.platform.TenantContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/** Returns the tenant bound to the request, so tests can check [TenantBindingFilter]. Outside the documented API paths. */
@RestController
class CurrentTenantTestController {

    @GetMapping(PATH)
    fun currentTenant(): Map<String, String?> = mapOf("tenantId" to TenantContext.currentTenantId()?.toString())

    companion object {
        const val PATH = "/test/current-tenant"
    }
}

package com.etude.tenant

/** Creates tenants. Sign-up uses it to create the company before its owner. */
interface TenantRegistration {

    /** Registers a tenant with a newly issued code. Call it as platform work: the tenant does not exist yet. */
    fun register(name: String): TenantInfo
}

package com.etude.tenant

/** Looks up tenants. Staff sign-in uses it to find the company of a company code. */
interface TenantDirectory {

    fun findByCode(code: String): TenantInfo?
}

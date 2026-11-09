package com.etude.tenant

/** 회사를 조회한다. */
interface TenantDirectory {

    fun findByCode(code: String): TenantInfo?
}

package com.etude.tenant

/** 회사를 찾는다. 직원 로그인이 회사 코드로 회사를 찾을 때 쓴다. */
interface TenantDirectory {

    fun findByCode(code: String): TenantInfo?
}

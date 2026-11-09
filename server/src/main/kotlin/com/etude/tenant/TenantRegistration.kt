package com.etude.tenant

/** 회사를 등록한다. */
interface TenantRegistration {

    /** 회사 코드를 발급해 회사를 등록한다. 플랫폼 블록에서 호출한다. */
    fun register(name: String): TenantInfo
}

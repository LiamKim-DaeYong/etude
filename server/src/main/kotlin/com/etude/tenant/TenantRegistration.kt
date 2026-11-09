package com.etude.tenant

/** 회사를 만든다. 회원가입이 대표보다 먼저 회사를 만들 때 쓴다. */
interface TenantRegistration {

    /** 새로 발급한 코드로 회사를 등록한다. 아직 회사가 없으므로 플랫폼 작업으로 부른다. */
    fun register(name: String): TenantInfo
}

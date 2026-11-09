package com.etude.platform.security

import com.etude.platform.TenantContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/** 요청의 현재 회사를 돌려준다. [TenantBindingFilter] 테스트용. */
@RestController
class CurrentTenantTestController {

    @GetMapping(PATH)
    fun currentTenant(): Map<String, String?> = mapOf("tenantId" to TenantContext.currentTenantId()?.toString())

    companion object {
        const val PATH = "/test/current-tenant"
    }
}

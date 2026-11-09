package com.etude.platform.security

import com.etude.platform.TenantContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/** 요청에 걸린 회사를 돌려줘 테스트가 [TenantBindingFilter]를 확인하게 한다. 문서화한 API 경로 밖에 둔다. */
@RestController
class CurrentTenantTestController {

    @GetMapping(PATH)
    fun currentTenant(): Map<String, String?> = mapOf("tenantId" to TenantContext.currentTenantId()?.toString())

    companion object {
        const val PATH = "/test/current-tenant"
    }
}

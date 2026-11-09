package com.etude.platform

import java.util.UUID

/** 회사에 속한 로그인 사용자. 요청은 [tenantId]의 회사로 실행된다. */
interface TenantPrincipal {
    val tenantId: UUID
}

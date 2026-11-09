package com.etude.platform

import java.util.UUID

/** 회사에 속한 로그인 주체. 보안 필터가 요청마다 이 [tenantId]로 실행한다. */
interface TenantPrincipal {
    val tenantId: UUID
}

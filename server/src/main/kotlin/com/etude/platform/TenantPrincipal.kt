package com.etude.platform

import java.util.UUID

/** 회사에 속한 로그인 사용자. 요청은 [tenantId]의 회사로 실행된다. */
interface TenantPrincipal {
    val tenantId: UUID

    /** 로그인한 사용자의 ID. 작성자 확인처럼 사용자를 가리킬 때 쓴다. */
    val userId: UUID
}

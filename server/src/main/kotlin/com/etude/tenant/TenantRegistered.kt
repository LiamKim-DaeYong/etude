package com.etude.tenant

import java.util.UUID

/**
 * 회사를 등록한 트랜잭션에서 발행한다. 다른 모듈은 이 이벤트로 회사의 초기 데이터를 준비한다.
 * 발행은 플랫폼 블록에서 하므로, 리스너는 [tenantId]의 회사로 전환해서 그 회사의 데이터를 만든다.
 */
data class TenantRegistered(val tenantId: UUID)

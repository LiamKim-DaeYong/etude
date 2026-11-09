package com.etude.platform

/**
 * 업무 규칙에 따른 거절. 응답 `code`는 [reason] 이름이고, 상태는 [RejectionStatuses]가 정한다.
 */
abstract class RejectedException(message: String) : RuntimeException(message) {

    abstract val reason: Enum<*>

    /** 응답 본문에 더할 필드(예: 현재 총액). */
    open val properties: Map<String, Any>
        get() = emptyMap()
}

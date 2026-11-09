package com.etude.platform

/**
 * 요청 형식은 올바르지만 업무 규칙상 처리할 수 없는 요청. API는 [reason] 이름을 `code`로 하는 Problem을 돌려주고,
 * 상태 코드는 사유 enum의 [RejectionStatuses]에서 정한다.
 */
abstract class RejectedException(message: String) : RuntimeException(message) {

    abstract val reason: Enum<*>

    /** 클라이언트가 대처하는 데 필요한 Problem 본문의 추가 필드(예: 현재 총액). */
    open val properties: Map<String, Any>
        get() = emptyMap()
}

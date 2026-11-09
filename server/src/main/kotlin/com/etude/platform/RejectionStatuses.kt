package com.etude.platform

import org.springframework.http.HttpStatus

/**
 * 거절 사유 enum의 사유별 HTTP 상태. 모듈은 enum마다 web 패키지에 빈 하나를 등록하고,
 * [RejectedException] 응답과 API 문서가 이 매핑 하나를 함께 쓴다.
 */
interface RejectionStatuses<E : Enum<E>> {
    val reasonType: Class<E>

    fun statusOf(reason: E): HttpStatus

    /** 실행 중에만 타입을 아는 사유에 쓰는 [statusOf]. */
    fun statusOfAny(reason: Enum<*>): HttpStatus = statusOf(reasonType.cast(reason))
}

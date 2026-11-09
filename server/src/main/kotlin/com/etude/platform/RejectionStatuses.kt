package com.etude.platform

import org.springframework.http.HttpStatus

/**
 * 거절 사유별 HTTP 상태. 모듈은 사유 enum마다 web 패키지에 빈을 하나 등록한다.
 * 응답과 API 문서가 이 매핑을 함께 쓴다.
 */
interface RejectionStatuses<E : Enum<E>> {
    val reasonType: Class<E>

    fun statusOf(reason: E): HttpStatus

    /** 타입을 모르는 사유의 [statusOf]. */
    fun statusOfAny(reason: Enum<*>): HttpStatus = statusOf(reasonType.cast(reason))
}

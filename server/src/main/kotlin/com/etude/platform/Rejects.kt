package com.etude.platform

import kotlin.reflect.KClass

/**
 * 핸들러 메서드가 주어진 사유 enum으로 요청을 거절할 수 있음을 문서에 남긴다. OpenAPI 문서는 모든 사유를
 * 업무 오류 `code`로, 모듈의 [RejectionStatuses]가 정한 상태 아래에 나열한다.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Rejects(val reasons: KClass<out Enum<*>>)

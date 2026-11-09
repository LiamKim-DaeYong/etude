package com.etude.platform

import kotlin.reflect.KClass

/**
 * 핸들러가 이 사유 enum으로 거절할 수 있음을 표시한다. API 문서에 사유가 상태별 오류 `code`로 나온다.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Rejects(val reasons: KClass<out Enum<*>>)

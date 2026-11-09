package com.etude.platform

/**
 * 조회가 행 잠금을 기다리는 최대 시간(`jakarta.persistence.lock.timeout` 힌트 값).
 * 시간을 넘기면 요청 스레드를 붙잡지 않고 `Retry-After`와 함께 503으로 응답한다(ADR-0003).
 */
const val LOCK_TIMEOUT_MILLIS = "2000"

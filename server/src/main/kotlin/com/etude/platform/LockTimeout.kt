package com.etude.platform

/**
 * 행 잠금 대기 시간(`jakarta.persistence.lock.timeout` 힌트 값). 넘기면 503으로 응답한다(ADR-0003).
 */
const val LOCK_TIMEOUT_MILLIS = "2000"

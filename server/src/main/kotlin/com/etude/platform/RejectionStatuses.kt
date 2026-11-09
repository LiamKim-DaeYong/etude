package com.etude.platform

import org.springframework.http.HttpStatus

/**
 * HTTP status of each reason of a rejection enum. A module registers one bean per enum in its web package, so the
 * responses to [RejectedException] and the API documentation share one mapping.
 */
interface RejectionStatuses<E : Enum<E>> {
    val reasonType: Class<E>

    fun statusOf(reason: E): HttpStatus

    /** [statusOf] for a reason whose type is only known at runtime. */
    fun statusOfAny(reason: Enum<*>): HttpStatus = statusOf(reasonType.cast(reason))
}

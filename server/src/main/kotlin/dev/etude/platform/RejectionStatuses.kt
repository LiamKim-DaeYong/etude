package dev.etude.platform

import org.springframework.http.HttpStatus

/**
 * HTTP status of each rejection reason of a module. A module's exception handler implements it and uses
 * [statusOf] for its responses, so the API documentation and the actual responses share one mapping.
 */
interface RejectionStatuses<E : Enum<E>> {
    val reasonType: Class<E>

    fun statusOf(reason: E): HttpStatus

    /** [statusOf] for a reason whose type is only known at runtime. */
    fun statusOfAny(reason: Enum<*>): HttpStatus = statusOf(reasonType.cast(reason))
}

package com.etude.platform

/**
 * A request that is valid but breaks a business rule. The API returns a problem whose `code` is the [reason] name
 * and whose status comes from the [RejectionStatuses] of the reason's enum type.
 */
abstract class RejectedException(message: String) : RuntimeException(message) {

    abstract val reason: Enum<*>

    /** Extra properties of the problem body that tell the client how to recover, such as the current total. */
    open val properties: Map<String, Any>
        get() = emptyMap()
}

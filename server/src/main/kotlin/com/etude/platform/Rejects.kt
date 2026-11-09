package com.etude.platform

import kotlin.reflect.KClass

/**
 * Documents that a handler method can reject a request with the given reason enum. The OpenAPI document lists
 * every reason as a business error `code`, under the status that the module's [RejectionStatuses] assigns.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Rejects(val reasons: KClass<out Enum<*>>)

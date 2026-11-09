package com.etude.platform.web

import com.etude.platform.RejectionStatuses
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

/** Finds the [RejectionStatuses] bean of a rejection reason enum. */
@Component
class RejectionStatusRegistry(statuses: List<RejectionStatuses<*>>) {

    private val byType: Map<Class<*>, RejectionStatuses<*>> = statuses.associateBy { it.reasonType }

    init {
        require(byType.size == statuses.size) { "More than one RejectionStatuses bean for the same reason type" }
    }

    fun forType(reasonType: Class<*>): RejectionStatuses<*> = byType[reasonType]
        ?: error("No RejectionStatuses bean for ${reasonType.name}")

    fun statusOf(reason: Enum<*>): HttpStatus = forType(reason.declaringJavaClass).statusOfAny(reason)
}

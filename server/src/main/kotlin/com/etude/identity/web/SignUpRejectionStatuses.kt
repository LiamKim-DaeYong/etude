package com.etude.identity.web

import com.etude.identity.application.SignUpRejectedException.Reason
import com.etude.platform.RejectionStatuses
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class SignUpRejectionStatuses : RejectionStatuses<Reason> {

    override val reasonType = Reason::class.java

    override fun statusOf(reason: Reason): HttpStatus = when (reason) {
        Reason.EMAIL_ALREADY_USED -> HttpStatus.CONFLICT
    }
}

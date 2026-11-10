package com.etude.board.web

import com.etude.board.application.PostRejectedException.Reason
import com.etude.platform.RejectionStatuses
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class PostRejectionStatuses : RejectionStatuses<Reason> {

    override val reasonType = Reason::class.java

    override fun statusOf(reason: Reason): HttpStatus = when (reason) {
        Reason.NOT_POST_AUTHOR -> HttpStatus.FORBIDDEN
    }
}

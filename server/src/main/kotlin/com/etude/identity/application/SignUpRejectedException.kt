package com.etude.identity.application

import com.etude.platform.RejectedException

class SignUpRejectedException(
    override val reason: Reason,
    message: String,
) : RejectedException(message) {

    enum class Reason {
        EMAIL_ALREADY_USED,
    }
}

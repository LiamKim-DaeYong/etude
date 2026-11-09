package com.etude.identity.web

import com.etude.identity.application.SignedInUser
import com.etude.identity.domain.UserRole
import java.util.UUID

data class SessionResponse(
    val userId: UUID,
    val role: UserRole,
    val username: String,
) {
    companion object {
        fun from(user: SignedInUser) = SessionResponse(user.userId, user.role, user.username)
    }
}

data class CsrfTokenResponse(
    val headerName: String,
    val token: String,
)

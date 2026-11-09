package com.etude.identity.application

import java.util.UUID

data class SignUpResult(
    val tenantId: UUID,
    val tenantCode: String,
    val userId: UUID,
)

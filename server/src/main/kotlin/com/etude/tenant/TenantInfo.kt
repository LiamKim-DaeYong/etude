package com.etude.tenant

import java.util.UUID

data class TenantInfo(
    val id: UUID,
    val name: String,
    val code: String,
)

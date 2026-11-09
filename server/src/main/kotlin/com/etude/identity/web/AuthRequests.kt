package com.etude.identity.web

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class OwnerLoginRequest(
    @field:NotBlank
    @field:Email
    @field:Size(max = 254)
    val email: String,
    @field:NotBlank
    val password: String,
)

data class StaffLoginRequest(
    @field:Pattern(regexp = "[0-9]{8}")
    val companyCode: String,
    @field:NotBlank
    @field:Size(max = 50)
    val username: String,
    @field:NotBlank
    val password: String,
)

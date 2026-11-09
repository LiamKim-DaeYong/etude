package com.etude.identity.application

data class SignUpCommand(
    val companyName: String,
    val email: String,
    val username: String,
    val password: String,
)

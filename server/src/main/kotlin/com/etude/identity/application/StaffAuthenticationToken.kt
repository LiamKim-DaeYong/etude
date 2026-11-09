package com.etude.identity.application

import org.springframework.security.authentication.AbstractAuthenticationToken

/** A staff sign-in request: company code, username and password. [StaffAuthenticationProvider] checks it. */
class StaffAuthenticationToken(
    val companyCode: String,
    val username: String,
    val password: String,
) : AbstractAuthenticationToken(emptyList()) {

    override fun getPrincipal(): Any = username

    override fun getCredentials(): Any = password
}

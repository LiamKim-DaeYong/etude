package com.etude.identity.application

import org.springframework.security.authentication.AbstractAuthenticationToken

/** 직원 로그인 요청. [StaffAuthenticationProvider]가 인증한다. */
class StaffAuthenticationToken(
    val companyCode: String,
    val username: String,
    val password: String,
) : AbstractAuthenticationToken(emptyList()) {

    override fun getPrincipal(): Any = username

    override fun getCredentials(): Any = password
}

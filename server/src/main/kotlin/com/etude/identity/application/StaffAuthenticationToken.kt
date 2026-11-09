package com.etude.identity.application

import org.springframework.security.authentication.AbstractAuthenticationToken

/** 직원 로그인 요청(회사 코드, 아이디, 비밀번호). [StaffAuthenticationProvider]가 확인한다. */
class StaffAuthenticationToken(
    val companyCode: String,
    val username: String,
    val password: String,
) : AbstractAuthenticationToken(emptyList()) {

    override fun getPrincipal(): Any = username

    override fun getCredentials(): Any = password
}

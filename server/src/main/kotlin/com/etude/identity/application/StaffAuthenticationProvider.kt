package com.etude.identity.application

import com.etude.identity.domain.UserRepository
import com.etude.identity.domain.UserRole
import com.etude.tenant.TenantDirectory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/** Checks staff sign-in: company code, then username within that company, then password. */
@Component
class StaffAuthenticationProvider(
    private val tenantDirectory: TenantDirectory,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val token = authentication as StaffAuthenticationToken

        val tenant = tenantDirectory.findByCode(token.companyCode) ?: throw invalidCredentials()
        val user = userRepository.findByTenantIdAndUsername(tenant.id, token.username)
            ?.takeIf { it.role == UserRole.STAFF }
            ?: throw invalidCredentials()
        if (!passwordEncoder.matches(token.password, user.passwordHash)) throw invalidCredentials()

        val principal = SignedInUser.of(user)
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.authorities)
    }

    override fun supports(authentication: Class<*>): Boolean = StaffAuthenticationToken::class.java.isAssignableFrom(authentication)

    private fun invalidCredentials() = BadCredentialsException("Invalid credentials")
}

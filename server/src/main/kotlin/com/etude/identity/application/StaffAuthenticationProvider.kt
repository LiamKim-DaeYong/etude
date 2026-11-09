package com.etude.identity.application

import com.etude.identity.domain.UserRepository
import com.etude.tenant.TenantDirectory
import org.springframework.security.authentication.AuthenticationProvider
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

    /**
     * 1. Find the company with `tenantDirectory.findByCode(token.companyCode)`.
     * 2. Find the user with `userRepository.findByTenantIdAndUsername(...)`. Only staff may sign in this way.
     * 3. Check the password with `passwordEncoder.matches(token.password, user.passwordHash)`.
     * 4. On success return `UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.authorities)`
     *    with `principal = SignedInUser.of(user)`.
     *
     * When any step fails, throw `BadCredentialsException("Invalid credentials")`: the same exception for every
     * failure, so callers cannot tell a wrong company code from a wrong password.
     */
    override fun authenticate(authentication: Authentication): Authentication {
        val token = authentication as StaffAuthenticationToken
        TODO("Check the company code, username and password")
    }

    override fun supports(authentication: Class<*>): Boolean = StaffAuthenticationToken::class.java.isAssignableFrom(authentication)
}

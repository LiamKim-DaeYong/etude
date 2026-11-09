package com.etude.identity.application

import com.etude.identity.domain.UserRepository
import com.etude.identity.domain.UserRole
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Finds the owner for owner sign-in (email and password). Spring Security's `DaoAuthenticationProvider` calls it,
 * then checks the password against the returned hash.
 */
@Service
class OwnerUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email.lowercase())
            ?.takeIf { it.role == UserRole.OWNER }
            ?: throw UsernameNotFoundException("Invalid credentials")
        return SignedInUser.of(user)
    }
}

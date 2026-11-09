package com.etude.identity.application

import com.etude.identity.domain.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

/**
 * Finds the owner for owner sign-in (email and password). Spring Security's `DaoAuthenticationProvider` calls it,
 * then checks the password against the returned hash.
 */
@Service
class OwnerUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    /**
     * 1. Find the user by the lowercased email with `userRepository.findByEmail(...)`.
     * 2. Only an owner may sign in this way.
     * 3. Return `SignedInUser.of(user)`; otherwise throw `UsernameNotFoundException`.
     *    Spring Security reports it as a wrong password, so callers cannot tell which emails exist.
     */
    override fun loadUserByUsername(email: String): UserDetails {
        TODO("Find the owner by email")
    }
}

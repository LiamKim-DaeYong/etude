package com.etude.identity.application

import com.etude.identity.domain.UserRepository
import com.etude.identity.domain.UserRole
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * 대표 로그인(이메일, 비밀번호)에서 대표를 찾는다. Spring Security의 `DaoAuthenticationProvider`가 이 메서드를 부르고,
 * 돌려받은 해시로 비밀번호를 확인한다.
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

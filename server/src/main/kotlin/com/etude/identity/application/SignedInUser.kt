package com.etude.identity.application

import com.etude.identity.domain.User
import com.etude.identity.domain.UserRole
import com.etude.platform.TenantPrincipal
import org.springframework.security.core.CredentialsContainer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable
import java.util.UUID

/**
 * The signed-in user kept in the session. Holds the password hash only until sign-in checks it; Spring Security
 * erases it afterwards ([eraseCredentials]).
 */
class SignedInUser(
    val userId: UUID,
    override val tenantId: UUID,
    val role: UserRole,
    private val name: String,
    private var passwordHash: String?,
) : UserDetails,
    CredentialsContainer,
    TenantPrincipal,
    Serializable {

    override fun getUsername(): String = name

    override fun getPassword(): String? = passwordHash

    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun eraseCredentials() {
        passwordHash = null
    }

    companion object {
        fun of(user: User) = SignedInUser(checkNotNull(user.id), user.tenantId, user.role, user.username, user.passwordHash)
    }
}

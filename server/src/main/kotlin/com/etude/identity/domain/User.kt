package com.etude.identity.domain

import com.etude.shared.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.util.UUID

/**
 * A person who signs in to a tenant. Not tenant-scoped: sign-in finds the user before the tenant is known,
 * so the tenant is an ordinary column (ADR-0006).
 */
@Entity
@Table(name = "users")
class User(
    tenantId: UUID,
    role: UserRole,
    username: String,
    email: String?,
    passwordHash: String,
) : BaseEntity() {

    @Column(nullable = false, updatable = false)
    var tenantId: UUID = tenantId
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: UserRole = role
        protected set

    @Column(nullable = false, length = 50)
    var username: String = username
        protected set

    @Column(length = 254)
    var email: String? = email?.lowercase()
        protected set

    @Column(nullable = false)
    var passwordHash: String = passwordHash
        protected set

    init {
        require(role != UserRole.OWNER || email != null) { "An owner needs an email" }
    }
}

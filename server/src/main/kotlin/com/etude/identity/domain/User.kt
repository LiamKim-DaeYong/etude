package com.etude.identity.domain

import com.etude.shared.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.util.UUID

/**
 * 회사에 속해 로그인하는 사용자.
 *
 * 로그인할 때 회사보다 먼저 조회하므로 테넌트 데이터가 아니고, 소속 회사를 일반 칼럼으로 가진다(ADR-0006).
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

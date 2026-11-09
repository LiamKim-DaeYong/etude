package com.etude.identity.domain

import org.springframework.data.repository.Repository
import java.util.UUID

interface UserRepository : Repository<User, UUID> {

    fun save(user: User): User

    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): User?

    fun findByTenantIdAndUsername(tenantId: UUID, username: String): User?
}

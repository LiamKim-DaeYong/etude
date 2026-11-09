package com.etude.tenant.application

import com.etude.tenant.TenantInfo
import com.etude.tenant.TenantRegistration
import com.etude.tenant.domain.TenantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom

@Service
@Transactional(readOnly = true)
class TenantService(
    private val tenantRepository: TenantRepository,
) : TenantRegistration {

    private val random = SecureRandom()

    /**
     * Saves `Tenant(name, issueCode())` and returns it as [TenantInfo] (id, name, code).
     * `BaseEntity.id` is nullable until saved, so read it from the saved entity.
     */
    @Transactional
    override fun register(name: String): TenantInfo {
        TODO("Save the tenant with a new code and return its TenantInfo")
    }

    /**
     * An unused 8-digit code: draw a number from 10,000,000 to 99,999,999 with [random], and draw again while
     * `tenantRepository.existsByCode(...)` finds it. Give up with an error after [MAX_ATTEMPTS] draws.
     */
    private fun issueCode(): String {
        TODO("Issue an unused 8-digit code")
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}

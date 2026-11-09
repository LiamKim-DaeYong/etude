package com.etude.tenant.application

import com.etude.tenant.TenantInfo
import com.etude.tenant.TenantRegistration
import com.etude.tenant.domain.Tenant
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

    @Transactional
    override fun register(name: String): TenantInfo {
        val tenant = tenantRepository.save(Tenant(name, issueCode()))
        return TenantInfo(checkNotNull(tenant.id), tenant.name, tenant.code)
    }

    private fun issueCode(): String {
        repeat(MAX_ATTEMPTS) {
            val code = random.nextInt(10_000_000, 100_000_000).toString()
            if (!tenantRepository.existsByCode(code)) return code
        }
        error("No unused tenant code after $MAX_ATTEMPTS attempts")
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}

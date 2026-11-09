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

    /**
     * An unused 8-digit code. Two sign-ups that draw the same code at the same moment both pass the check, and the
     * unique constraint rejects the later one; with 90 million codes this is left as a failed sign-up.
     */
    private fun issueCode(): String = generateSequence { (CODE_MIN + random.nextInt(CODE_RANGE)).toString() }
        .take(MAX_ATTEMPTS)
        .firstOrNull { !tenantRepository.existsByCode(it) }
        ?: error("No unused tenant code after $MAX_ATTEMPTS attempts")

    private companion object {
        const val CODE_MIN = 10_000_000
        const val CODE_RANGE = 90_000_000
        const val MAX_ATTEMPTS = 5
    }
}

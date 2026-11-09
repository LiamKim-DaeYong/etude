package com.etude.identity.application

import com.etude.identity.application.SignUpRejectedException.Reason
import com.etude.identity.domain.User
import com.etude.identity.domain.UserRepository
import com.etude.identity.domain.UserRole
import com.etude.platform.TenantContext
import com.etude.tenant.TenantRegistration
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class SignUpService(
    private val tenantRegistration: TenantRegistration,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val transactionTemplate: TransactionTemplate,
) {

    fun signUp(command: SignUpCommand): SignUpResult = TenantContext.runAsPlatform {
        checkNotNull(
            transactionTemplate.execute {
                val email = command.email.lowercase()
                if (userRepository.existsByEmail(email)) {
                    throw SignUpRejectedException(Reason.EMAIL_ALREADY_USED, "Email already used: $email")
                }

                val tenant = tenantRegistration.register(command.companyName)
                val owner = userRepository.save(
                    User(tenant.id, UserRole.OWNER, command.username, email, checkNotNull(passwordEncoder.encode(command.password))),
                )
                SignUpResult(tenant.id, tenant.code, checkNotNull(owner.id))
            },
        )
    }
}

package com.etude.identity.application

import com.etude.identity.domain.UserRepository
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

    /**
     * Creates a company (tenant) and its owner in one transaction.
     *
     * 1. Run as platform work with `TenantContext.runAsPlatform { }`: the company does not exist yet.
     *    Bind it outside the transaction, because Hibernate reads the tenant when the transaction starts.
     * 2. Inside it, run one transaction with `transactionTemplate.execute { }`:
     *    - Reject with `SignUpRejectedException(Reason.EMAIL_ALREADY_USED, ...)` when the email is taken.
     *      Compare the lowercased email: `User` stores emails in lowercase.
     *    - Create the company with `tenantRegistration.register(...)`.
     *    - Save the owner: `User(tenant.id, UserRole.OWNER, ..., passwordEncoder.encode(password))`.
     *      Never store the raw password.
     * 3. Return the company id, the company code and the owner id.
     */
    fun signUp(command: SignUpCommand): SignUpResult {
        TODO("Write the sign-up flow described above")
    }
}

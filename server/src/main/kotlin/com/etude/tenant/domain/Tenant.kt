package com.etude.tenant.domain

import com.etude.shared.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Tenant(
    name: String,
    code: String,
) : BaseEntity() {

    @Column(nullable = false, length = 200)
    var name: String = name
        protected set

    /** Issued by the system and never changed; staff type it to sign in. Not the tenant id (ADR-0006). */
    @Column(nullable = false, updatable = false, length = 8)
    var code: String = code
        protected set
}

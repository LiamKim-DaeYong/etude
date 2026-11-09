package com.etude.shared

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.TenantId
import java.util.UUID

@MappedSuperclass
abstract class TenantScopedEntity : BaseEntity() {

    @TenantId
    @Column(nullable = false, updatable = false)
    var tenantId: UUID? = null
        protected set
}

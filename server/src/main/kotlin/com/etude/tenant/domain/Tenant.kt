package com.etude.tenant.domain

import com.etude.shared.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Tenant(
    name: String,
) : BaseEntity() {

    @Column(nullable = false, length = 200)
    var name: String = name
        protected set
}

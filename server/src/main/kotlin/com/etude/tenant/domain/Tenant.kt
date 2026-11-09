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

    /** 시스템이 발급하고 바꾸지 않는다. 직원이 로그인할 때 입력한다. 테넌트 ID와 다르다(ADR-0006). */
    @Column(nullable = false, updatable = false, length = 8)
    var code: String = code
        protected set
}

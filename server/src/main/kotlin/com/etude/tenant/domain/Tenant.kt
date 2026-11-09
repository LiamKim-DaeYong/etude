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

    /** 직원 로그인에 쓰는 회사 코드. 발급 후 바뀌지 않는다. */
    @Column(nullable = false, updatable = false, length = 8)
    var code: String = code
        protected set
}

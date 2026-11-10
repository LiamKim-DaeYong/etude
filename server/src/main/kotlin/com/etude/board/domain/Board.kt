package com.etude.board.domain

import com.etude.shared.TenantScopedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

/**
 * 회사의 게시판. 첫 버전은 회사마다 기본 게시판 하나만 있다.
 */
@Entity
class Board private constructor(
    name: String?,
    isDefault: Boolean,
) : TenantScopedEntity() {

    /** 게시판 이름. 기본 게시판은 `null`이고 클라이언트가 언어에 맞는 이름을 보여 준다. */
    @Column(length = 100)
    var name: String? = name
        protected set

    @Column(nullable = false, updatable = false)
    var isDefault: Boolean = isDefault
        protected set

    companion object {
        fun default() = Board(name = null, isDefault = true)
    }
}

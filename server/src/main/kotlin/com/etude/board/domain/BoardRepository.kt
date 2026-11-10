package com.etude.board.domain

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import java.util.UUID

/** 현재 회사의 게시판만 다룬다(`@TenantId`, RLS). */
interface BoardRepository : Repository<Board, UUID> {

    fun save(board: Board): Board

    fun existsById(id: UUID): Boolean

    @Query("select b from Board b where b.isDefault = true")
    fun findDefault(): Board?

    /** 기본 게시판을 먼저, 나머지는 만든 순서로. */
    @Query("select b from Board b order by b.isDefault desc, b.createdAt, b.id")
    fun findAllInDisplayOrder(): List<Board>
}

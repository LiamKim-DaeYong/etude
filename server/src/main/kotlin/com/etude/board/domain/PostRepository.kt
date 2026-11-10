package com.etude.board.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import java.util.UUID

/** 현재 회사의 게시글만 다룬다(`@TenantId`, RLS). 삭제한 글은 조회하지 않는다. */
interface PostRepository : Repository<Post, UUID> {

    fun save(post: Post): Post

    /** 게시판이 다르면 같은 회사의 글이라도 찾지 않는다. */
    @Query("select p from Post p where p.id = :id and p.boardId = :boardId and p.deletedAt is null")
    fun findInBoard(id: UUID, boardId: UUID): Post?

    fun findByBoardIdAndDeletedAtIsNull(boardId: UUID, pageable: Pageable): Page<Post>

    fun findByDeletedAtIsNull(pageable: Pageable): List<Post>
}

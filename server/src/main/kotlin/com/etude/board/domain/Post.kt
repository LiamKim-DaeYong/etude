package com.etude.board.domain

import com.etude.shared.TenantScopedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import java.time.Instant
import java.util.UUID

/**
 * 게시판의 글. 본문은 일반 텍스트다. 삭제하면 [deletedAt]만 남기고 목록과 상세에서 뺀다.
 */
@Entity
class Post(
    boardId: UUID,
    authorId: UUID,
    title: String,
    body: String,
    postedAt: Instant,
) : TenantScopedEntity() {

    @Column(nullable = false, updatable = false)
    var boardId: UUID = boardId
        protected set

    /** 글을 쓴 사용자. 수정과 삭제는 작성자만 한다. */
    @Column(nullable = false, updatable = false)
    var authorId: UUID = authorId
        protected set

    @Column(nullable = false, length = 200)
    var title: String = title
        protected set

    @Column(nullable = false, columnDefinition = "text")
    var body: String = body
        protected set

    @Column(nullable = false, updatable = false)
    var postedAt: Instant = postedAt
        protected set

    @Column
    var editedAt: Instant? = null
        protected set

    @Column
    var deletedAt: Instant? = null
        protected set

    fun isWrittenBy(userId: UUID): Boolean = authorId == userId

    fun edit(title: String, body: String, editedAt: Instant) {
        check(deletedAt == null) { "Cannot edit a deleted post: $id" }
        this.title = title
        this.body = body
        this.editedAt = editedAt
    }

    fun delete(deletedAt: Instant) {
        check(this.deletedAt == null) { "Post already deleted: $id" }
        this.deletedAt = deletedAt
    }
}

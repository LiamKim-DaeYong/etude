package com.etude.board.application

import java.time.Instant
import java.util.UUID

/** 게시글 상세. [editable]은 조회한 사용자가 작성자라 수정·삭제할 수 있는지다. */
data class PostDetails(
    val id: UUID,
    val boardId: UUID,
    val title: String,
    val body: String,
    val authorId: UUID,
    val authorUsername: String,
    val postedAt: Instant,
    val editedAt: Instant?,
    val editable: Boolean,
)

/** 목록에 보이는 게시글. 본문은 뺀다. */
data class PostSummary(
    val id: UUID,
    val boardId: UUID,
    val title: String,
    val authorId: UUID,
    val authorUsername: String,
    val postedAt: Instant,
    val editedAt: Instant?,
)

/** 게시글 목록의 한 페이지. [page]는 0부터 센다. */
data class PostPage(
    val items: List<PostSummary>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

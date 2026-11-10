package com.etude.board.web

import com.etude.board.application.BoardDetails
import com.etude.board.application.PostDetails
import com.etude.board.application.PostPage
import com.etude.board.application.PostSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

data class BoardResponse(
    val id: UUID,
    @field:Schema(description = "Null for the default board; the client shows a localized name.")
    val name: String?,
    @field:Schema(description = "Whether this is the company's default board.")
    val default: Boolean,
) {
    companion object {
        fun from(board: BoardDetails) = BoardResponse(board.id, board.name, board.isDefault)
    }
}

data class PostResponse(
    val id: UUID,
    val boardId: UUID,
    val title: String,
    @field:Schema(description = "Plain text. Render as text, not HTML.")
    val body: String,
    val authorId: UUID,
    val authorUsername: String,
    val postedAt: Instant,
    @field:Schema(description = "Null if the post was never edited.")
    val editedAt: Instant?,
    @field:Schema(description = "Whether the signed-in user is the author and can edit or delete the post.")
    val editable: Boolean,
) {
    companion object {
        fun from(post: PostDetails) = PostResponse(
            id = post.id,
            boardId = post.boardId,
            title = post.title,
            body = post.body,
            authorId = post.authorId,
            authorUsername = post.authorUsername,
            postedAt = post.postedAt,
            editedAt = post.editedAt,
            editable = post.editable,
        )
    }
}

@Schema(name = "PostSummaryResponse")
data class PostSummaryResponse(
    val id: UUID,
    val boardId: UUID,
    val title: String,
    val authorId: UUID,
    val authorUsername: String,
    val postedAt: Instant,
    val editedAt: Instant?,
) {
    companion object {
        fun from(post: PostSummary) = PostSummaryResponse(
            id = post.id,
            boardId = post.boardId,
            title = post.title,
            authorId = post.authorId,
            authorUsername = post.authorUsername,
            postedAt = post.postedAt,
            editedAt = post.editedAt,
        )
    }
}

data class PostPageResponse(
    val items: List<PostSummaryResponse>,
    @field:Schema(description = "Zero-based page number.")
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    companion object {
        fun from(page: PostPage) = PostPageResponse(
            items = page.items.map(PostSummaryResponse::from),
            page = page.page,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }
}

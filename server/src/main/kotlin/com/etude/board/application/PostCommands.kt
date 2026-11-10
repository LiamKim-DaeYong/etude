package com.etude.board.application

import java.util.UUID

data class WritePostCommand(
    val boardId: UUID,
    val authorId: UUID,
    val title: String,
    val body: String,
)

data class EditPostCommand(
    val boardId: UUID,
    val postId: UUID,
    val userId: UUID,
    val title: String,
    val body: String,
)

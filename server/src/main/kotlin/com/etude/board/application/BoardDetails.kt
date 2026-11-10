package com.etude.board.application

import com.etude.board.domain.Board
import java.util.UUID

data class BoardDetails(
    val id: UUID,
    val name: String?,
    val isDefault: Boolean,
) {
    companion object {
        fun from(board: Board) = BoardDetails(checkNotNull(board.id), board.name, board.isDefault)
    }
}

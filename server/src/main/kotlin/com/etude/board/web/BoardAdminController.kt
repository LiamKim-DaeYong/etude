package com.etude.board.web

import com.etude.board.application.BoardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Boards")
@RequestMapping("/admin/v1/boards")
class BoardAdminController(
    private val boardService: BoardService,
) {

    @GetMapping
    @Operation(
        summary = "List the company's boards",
        description = "The default board comes first. It is prepared when the company is registered; " +
            "until then the list is empty. This request never creates a board.",
    )
    fun listBoards(): List<BoardResponse> = boardService.listBoards().map(BoardResponse::from)
}

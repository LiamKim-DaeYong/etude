package com.etude.board.web

import com.etude.board.application.EditPostCommand
import com.etude.board.application.PostRejectedException
import com.etude.board.application.PostService
import com.etude.board.application.WritePostCommand
import com.etude.platform.Rejects
import com.etude.platform.TenantPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.UUID

/**
 * 게시글 API. 회사는 로그인한 사용자의 회사로 정해지고, 다른 회사나 다른 게시판의 ID는 없는 것과 같게 404다.
 */
@RestController
@Tag(name = "Posts")
@RequestMapping("/admin/v1")
class PostAdminController(
    private val postService: PostService,
) {

    @GetMapping("/boards/{boardId}/posts")
    @Operation(summary = "List posts on a board", description = "Newest first. `page` starts at 0. Bodies are left out.")
    fun listPosts(
        @PathVariable boardId: UUID,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) size: Int,
    ): PostPageResponse = postService.listPosts(boardId, page, size)
        ?.let(PostPageResponse::from)
        ?: throw boardNotFound(boardId)

    @GetMapping("/posts")
    @Operation(summary = "List recent posts", description = "Newest posts across all of the company's boards, for the home screen.")
    fun listRecentPosts(
        @RequestParam(defaultValue = "5") @Min(1) @Max(MAX_RECENT_SIZE) size: Int,
    ): List<PostSummaryResponse> = postService.listRecentPosts(size).map(PostSummaryResponse::from)

    @PostMapping("/boards/{boardId}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Write a post", description = "The body is plain text.")
    fun writePost(
        @PathVariable boardId: UUID,
        @Valid @RequestBody request: WritePostRequest,
        @Parameter(hidden = true) @AuthenticationPrincipal user: TenantPrincipal,
    ): ResponseEntity<PostResponse> {
        val post = postService.write(WritePostCommand(boardId, user.userId, request.title, request.body))
            ?: throw boardNotFound(boardId)
        return ResponseEntity
            .created(URI.create("/admin/v1/boards/$boardId/posts/${post.id}"))
            .body(PostResponse.from(post))
    }

    @GetMapping("/boards/{boardId}/posts/{postId}")
    @Operation(summary = "Get a post")
    fun getPost(
        @PathVariable boardId: UUID,
        @PathVariable postId: UUID,
        @Parameter(hidden = true) @AuthenticationPrincipal user: TenantPrincipal,
    ): PostResponse = postService.findPost(boardId, postId, user.userId)
        ?.let(PostResponse::from)
        ?: throw postNotFound(postId)

    @PutMapping("/boards/{boardId}/posts/{postId}")
    @Operation(summary = "Edit a post", description = "Replaces the title and body. Only the author can edit.")
    @Rejects(PostRejectedException.Reason::class)
    fun editPost(
        @PathVariable boardId: UUID,
        @PathVariable postId: UUID,
        @Valid @RequestBody request: EditPostRequest,
        @Parameter(hidden = true) @AuthenticationPrincipal user: TenantPrincipal,
    ): PostResponse = postService.edit(EditPostCommand(boardId, postId, user.userId, request.title, request.body))
        ?.let(PostResponse::from)
        ?: throw postNotFound(postId)

    @DeleteMapping("/boards/{boardId}/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a post", description = "Only the author can delete. A deleted post is no longer listed or found.")
    @Rejects(PostRejectedException.Reason::class)
    fun deletePost(
        @PathVariable boardId: UUID,
        @PathVariable postId: UUID,
        @Parameter(hidden = true) @AuthenticationPrincipal user: TenantPrincipal,
    ) {
        if (!postService.delete(boardId, postId, user.userId)) throw postNotFound(postId)
    }

    private fun boardNotFound(boardId: UUID) = ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found: $boardId")

    private fun postNotFound(postId: UUID) = ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: $postId")

    private companion object {
        const val MAX_PAGE_SIZE = 100L
        const val MAX_RECENT_SIZE = 20L
    }
}

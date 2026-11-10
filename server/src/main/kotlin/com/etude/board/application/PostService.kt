package com.etude.board.application

import com.etude.board.application.PostRejectedException.Reason
import com.etude.board.domain.BoardRepository
import com.etude.board.domain.Post
import com.etude.board.domain.PostRepository
import com.etude.identity.UserDirectory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.UUID

/**
 * 현재 회사의 게시글. 같은 회사 사용자는 모두 읽고 쓰고, 수정·삭제는 작성자만 한다.
 * 다른 회사나 다른 게시판의 글은 없는 글과 같게 다룬다.
 */
@Service
@Transactional(readOnly = true)
class PostService(
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
    private val userDirectory: UserDirectory,
    private val clock: Clock,
) {

    /** 게시판이 없으면 `null`. 최신 글부터. */
    fun listPosts(boardId: UUID, page: Int, size: Int): PostPage? {
        if (!boardRepository.existsById(boardId)) return null
        val posts = postRepository.findByBoardIdAndDeletedAtIsNull(boardId, PageRequest.of(page, size, NEWEST_FIRST))
        return PostPage(
            items = summarize(posts.content),
            page = page,
            size = size,
            totalElements = posts.totalElements,
            totalPages = posts.totalPages,
        )
    }

    /** 회사의 모든 게시판에서 최신 글부터 [size]개. */
    fun listRecentPosts(size: Int): List<PostSummary> = summarize(postRepository.findByDeletedAtIsNull(PageRequest.of(0, size, NEWEST_FIRST)))

    fun findPost(boardId: UUID, postId: UUID, viewerId: UUID): PostDetails? = postRepository.findInBoard(postId, boardId)?.let { details(it, viewerId) }

    /** 게시판이 없으면 `null`. */
    @Transactional
    fun write(command: WritePostCommand): PostDetails? {
        if (!boardRepository.existsById(command.boardId)) return null
        val post = postRepository.save(Post(command.boardId, command.authorId, command.title, command.body, Instant.now(clock)))
        return details(post, command.authorId)
    }

    /** 글이 없으면 `null`. 작성자가 아니면 거절한다. */
    @Transactional
    fun edit(command: EditPostCommand): PostDetails? {
        val post = postRepository.findInBoard(command.postId, command.boardId) ?: return null
        requireAuthor(post, command.userId)
        post.edit(command.title, command.body, Instant.now(clock))
        return details(post, command.userId)
    }

    /** 글이 없으면 `false`. 작성자가 아니면 거절한다. */
    @Transactional
    fun delete(boardId: UUID, postId: UUID, userId: UUID): Boolean {
        val post = postRepository.findInBoard(postId, boardId) ?: return false
        requireAuthor(post, userId)
        post.delete(Instant.now(clock))
        return true
    }

    private fun requireAuthor(post: Post, userId: UUID) {
        if (!post.isWrittenBy(userId)) {
            throw PostRejectedException(Reason.NOT_POST_AUTHOR, "Only the author can change post ${post.id}")
        }
    }

    private fun details(post: Post, viewerId: UUID) = PostDetails(
        id = checkNotNull(post.id),
        boardId = post.boardId,
        title = post.title,
        body = post.body,
        authorId = post.authorId,
        authorUsername = usernames(listOf(post.authorId)).getValue(post.authorId),
        postedAt = post.postedAt,
        editedAt = post.editedAt,
        editable = post.isWrittenBy(viewerId),
    )

    private fun summarize(posts: List<Post>): List<PostSummary> {
        val usernames = usernames(posts.map { it.authorId })
        return posts.map {
            PostSummary(
                id = checkNotNull(it.id),
                boardId = it.boardId,
                title = it.title,
                authorId = it.authorId,
                authorUsername = usernames.getValue(it.authorId),
                postedAt = it.postedAt,
                editedAt = it.editedAt,
            )
        }
    }

    /** 사용자는 삭제하지 않으므로 작성자는 항상 있다. */
    private fun usernames(authorIds: Collection<UUID>): Map<UUID, String> = userDirectory.findAll(authorIds.toSet()).associate { it.id to it.username }

    private companion object {
        /** 같은 시각이면 나중에 만든 글(UUIDv7)이 먼저다. */
        val NEWEST_FIRST: Sort = Sort.by(Sort.Order.desc("postedAt"), Sort.Order.desc("id"))
    }
}

package com.etude.board.application

import com.etude.board.domain.Board
import com.etude.board.domain.BoardRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 현재 회사의 게시판. 회사는 요청의 테넌트로 정해진다. */
@Service
@Transactional(readOnly = true)
class BoardService(
    private val boardRepository: BoardRepository,
) {

    /**
     * 현재 회사의 기본 게시판을 준비한다. 이미 있으면 그대로 돌려준다.
     * 동시에 준비하면 한쪽이 유일 인덱스 위반으로 실패하고, 기본 게시판은 하나만 남는다.
     */
    @Transactional
    fun prepareDefaultBoard(): BoardDetails {
        val board = boardRepository.findDefault() ?: boardRepository.save(Board.default())
        return BoardDetails.from(board)
    }

    fun findDefaultBoard(): BoardDetails? = boardRepository.findDefault()?.let(BoardDetails::from)

    /** 조회만 한다. 기본 게시판이 아직 없으면 빈 목록이다. */
    fun listBoards(): List<BoardDetails> = boardRepository.findAllInDisplayOrder().map(BoardDetails::from)
}

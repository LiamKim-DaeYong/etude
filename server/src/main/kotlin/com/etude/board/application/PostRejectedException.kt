package com.etude.board.application

import com.etude.platform.RejectedException

class PostRejectedException(
    override val reason: Reason,
    message: String,
) : RejectedException(message) {

    enum class Reason {
        /** 작성자가 아닌 사용자가 글을 수정하거나 삭제하려 했다. */
        NOT_POST_AUTHOR,
    }
}

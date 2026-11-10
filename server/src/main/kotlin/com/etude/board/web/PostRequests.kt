package com.etude.board.web

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class WritePostRequest(
    @field:NotBlank
    @field:Size(max = TITLE_MAX_LENGTH)
    val title: String,
    @field:NotBlank
    @field:Size(max = BODY_MAX_LENGTH)
    val body: String,
)

data class EditPostRequest(
    @field:NotBlank
    @field:Size(max = TITLE_MAX_LENGTH)
    val title: String,
    @field:NotBlank
    @field:Size(max = BODY_MAX_LENGTH)
    val body: String,
)

/** 길이는 UTF-16 코드 단위로 센다. 코드 포인트로 세는 데이터베이스 칼럼(`varchar(200)`)을 넘지 않는다. */
private const val TITLE_MAX_LENGTH = 200
private const val BODY_MAX_LENGTH = 20_000

package com.etude.identity

import java.util.UUID

/** 사용자를 조회한다. */
interface UserDirectory {

    /** 있는 사용자만 돌려준다. 순서는 [ids]와 관계없다. */
    fun findAll(ids: Collection<UUID>): List<UserInfo>
}

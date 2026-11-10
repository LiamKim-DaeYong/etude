package com.etude.identity.application

import com.etude.identity.UserDirectory
import com.etude.identity.UserInfo
import com.etude.identity.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
) : UserDirectory {

    override fun findAll(ids: Collection<UUID>): List<UserInfo> {
        if (ids.isEmpty()) return emptyList()
        return userRepository.findAllByIdIn(ids).map { UserInfo(checkNotNull(it.id), it.username) }
    }
}

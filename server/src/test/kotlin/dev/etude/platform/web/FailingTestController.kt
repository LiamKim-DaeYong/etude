package dev.etude.platform.web

import org.springframework.dao.CannotAcquireLockException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/** Throws exceptions so tests can check the 500 and 503 responses. Outside the documented API paths. */
@RestController
class FailingTestController {

    @GetMapping(PATH)
    fun fail(): String = throw IllegalStateException("internal detail that must not leak")

    @GetMapping(LOCK_TIMEOUT_PATH)
    fun timeOutOnLock(): String = throw CannotAcquireLockException("lock timeout")

    companion object {
        const val PATH = "/test/unexpected-failure"
        const val LOCK_TIMEOUT_PATH = "/test/lock-timeout"
    }
}

package dev.etude.platform.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/** Throws an unexpected exception so tests can check the 500 response. Outside the documented API paths. */
@RestController
class FailingTestController {

    @GetMapping(PATH)
    fun fail(): String = throw IllegalStateException("internal detail that must not leak")

    companion object {
        const val PATH = "/test/unexpected-failure"
    }
}

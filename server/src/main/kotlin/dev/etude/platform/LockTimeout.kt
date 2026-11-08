package dev.etude.platform

/**
 * How long a query waits for a row lock before giving up, as a `jakarta.persistence.lock.timeout` hint value.
 * A timeout becomes 503 with `Retry-After` instead of holding the request thread; see ADR-0003.
 */
const val LOCK_TIMEOUT_MILLIS = "2000"

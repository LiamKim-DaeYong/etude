package dev.etude

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Optional

/**
 * Single time source for the application.
 *
 * PostgreSQL `timestamptz` stores microseconds while `Instant.now()` can carry nanoseconds, so an entity's
 * in-memory time would differ from the stored one after a reload. The clock ticks in microseconds to match.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
class TimeConfiguration {

    @Bean
    fun clock(): Clock = Clock.tick(Clock.systemUTC(), Duration.ofNanos(1_000))

    @Bean
    fun auditingDateTimeProvider(clock: Clock): DateTimeProvider = DateTimeProvider { Optional.of(Instant.now(clock)) }
}

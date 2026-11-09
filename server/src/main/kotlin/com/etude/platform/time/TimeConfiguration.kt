package com.etude.platform.time

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Optional

/**
 * 애플리케이션의 시계.
 *
 * PostgreSQL `timestamptz` 정밀도에 맞춰 마이크로초 단위로 끊는다.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
class TimeConfiguration {

    @Bean
    fun clock(): Clock = Clock.tick(Clock.systemUTC(), Duration.ofNanos(1_000))

    @Bean
    fun auditingDateTimeProvider(clock: Clock): DateTimeProvider = DateTimeProvider { Optional.of(Instant.now(clock)) }
}

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
 * 애플리케이션의 유일한 시간 출처.
 *
 * PostgreSQL `timestamptz`는 마이크로초까지 저장하는데 `Instant.now()`는 나노초를 담을 수 있어, 다시 읽은 엔티티의
 * 시각이 메모리의 값과 달라진다. 그래서 시계를 마이크로초 단위로 맞춘다.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
class TimeConfiguration {

    @Bean
    fun clock(): Clock = Clock.tick(Clock.systemUTC(), Duration.ofNanos(1_000))

    @Bean
    fun auditingDateTimeProvider(clock: Clock): DateTimeProvider = DateTimeProvider { Optional.of(Instant.now(clock)) }
}

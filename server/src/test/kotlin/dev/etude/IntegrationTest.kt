package dev.etude

import dev.etude.platform.web.FailingTestController
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

/**
 * The only configuration for integration tests. Every test class that uses it shares one Spring context and
 * therefore one PostgreSQL container; a test that adds its own `@Import` or `@MockitoBean` would start another.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(TestcontainersConfiguration::class, FailingTestController::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
annotation class IntegrationTest

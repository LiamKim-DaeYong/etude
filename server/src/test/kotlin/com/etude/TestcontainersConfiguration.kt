package com.etude

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import java.nio.file.Files
import java.nio.file.Path

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer = PostgreSQLContainer(POSTGRES_IMAGE)

    companion object {
        /** Read from `compose.yaml` so local runs and tests use the same PostgreSQL version. */
        val POSTGRES_IMAGE: String = Regex("""image:\s*(postgres:\S+)""")
            .find(Files.readString(Path.of("compose.yaml")))
            ?.groupValues?.get(1)
            ?: error("No postgres image in compose.yaml")
    }
}

package com.etude

import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.MountableFile
import java.nio.file.Files
import java.nio.file.Path

/**
 * 테스트용 PostgreSQL. 로컬(`compose.yaml`)과 같은 이미지와 초기화 스크립트를 쓴다.
 *
 * 애플리케이션은 운영처럼 소유자가 아닌 역할 [APP_ROLE]로 접속해 RLS를 받는다.
 * Flyway는 `application-test.yml`의 컨테이너 소유자 역할로 접속한다.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    fun postgresContainer(): PostgreSQLContainer = PostgreSQLContainer(POSTGRES_IMAGE)
        .withCopyFileToContainer(MountableFile.forHostPath(INIT_SCRIPTS), "/docker-entrypoint-initdb.d/")

    @Bean
    fun jdbcConnectionDetails(postgres: PostgreSQLContainer): JdbcConnectionDetails = object : JdbcConnectionDetails {
        override fun getUsername(): String = APP_ROLE

        override fun getPassword(): String = APP_ROLE

        override fun getJdbcUrl(): String = postgres.jdbcUrl
    }

    companion object {
        /** 애플리케이션 역할. 이름과 비밀번호가 같다(`db/init`). */
        const val APP_ROLE = "etude_app"

        /** `compose.yaml`의 PostgreSQL 이미지. 로컬 실행과 같은 버전을 쓴다. */
        val POSTGRES_IMAGE: String = Regex("""image:\s*(postgres:\S+)""")
            .find(Files.readString(Path.of("compose.yaml")))
            ?.groupValues?.get(1)
            ?: error("No postgres image in compose.yaml")

        private val INIT_SCRIPTS: Path = Path.of("db", "init")
    }
}

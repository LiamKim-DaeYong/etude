package com.etude

import com.etude.platform.security.CurrentTenantTestController
import com.etude.platform.web.FailingTestController
import com.etude.platform.web.OpenApiContractConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

/**
 * 통합 테스트 설정. 모든 테스트가 컨텍스트와 PostgreSQL 컨테이너 하나를 공유한다.
 * 테스트 클래스에 `@Import`, `@MockitoBean`을 더하면 컨텍스트가 하나 더 뜬다.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(
    TestcontainersConfiguration::class,
    FailingTestController::class,
    CurrentTenantTestController::class,
    TestCompanies::class,
    OpenApiContractConfiguration::class,
)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
annotation class IntegrationTest

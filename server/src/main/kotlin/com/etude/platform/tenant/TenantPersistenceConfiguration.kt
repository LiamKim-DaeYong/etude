package com.etude.platform.tenant

import jakarta.persistence.EntityManagerFactory
import org.hibernate.cfg.SessionEventSettings
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.boot.transaction.autoconfigure.TransactionManagerCustomizers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

/**
 * 테넌트 격리의 영속성 설정(ADR-0006, ADR-0008): 트랜잭션별 회사 지정과 테넌트 데이터 쓰기 가드.
 */
@Configuration(proxyBeanMethods = false)
class TenantPersistenceConfiguration {

    /** Spring Boot의 기본 JPA 트랜잭션 관리자 대신 쓴다. */
    @Bean
    fun transactionManager(
        entityManagerFactory: EntityManagerFactory,
        customizers: ObjectProvider<TransactionManagerCustomizers>,
    ): PlatformTransactionManager = TenantTransactionManager(entityManagerFactory).also { manager ->
        customizers.ifAvailable { it.customize(manager) }
    }

    @Bean
    fun platformWriteGuard(): HibernatePropertiesCustomizer = HibernatePropertiesCustomizer {
        it[SessionEventSettings.INTERCEPTOR] = PlatformWriteGuard
    }
}

package com.etude.platform.tenant

import com.etude.platform.TenantContext
import com.etude.shared.TenantScopedEntity
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.hibernate.Session
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * 테넌트 데이터 저장소를 실제 회사가 정해진 작업에서만 쓰게 하는 공통 경계(ADR-0008).
 *
 * [TenantScopedEntity]를 다루는 모든 Spring Data 저장소에 자동으로 붙는다. 조회도 저장도 하기 전에 다음을 거절한다.
 * - 회사가 미지정이거나 플랫폼 블록이다. 회사 지정을 빠뜨린 코드가 조용히 빈 결과를 내지 않고 오류로 드러난다.
 * - 이미 열린 트랜잭션의 Hibernate 세션이 지금 회사와 다른 회사로 열렸다. 트랜잭션 안에서 회사를 바꾸면
 *   Spring은 기존 세션을 그대로 쓰므로, 그대로 두면 이전 회사의 행을 돌려준다.
 * 테넌트 데이터를 쓰는 모듈은 엔티티가 [TenantScopedEntity]를 상속하는 것만으로 같은 규칙을 받는다.
 */
@Component
class TenantRepositoryGuard : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        if (bean is RepositoryFactoryBeanSupport<*, *, *>) {
            bean.addRepositoryFactoryCustomizer { factory ->
                factory.addRepositoryProxyPostProcessor { proxyFactory, repository ->
                    if (TenantScopedEntity::class.java.isAssignableFrom(repository.domainType)) {
                        proxyFactory.addAdvice(RequireTenant)
                    }
                }
            }
        }
        return bean
    }

    /** 저장소의 트랜잭션 처리 안쪽에서 실행되므로 트랜잭션의 세션이 묶여 있다. */
    private object RequireTenant : MethodInterceptor {
        override fun invoke(invocation: MethodInvocation): Any? {
            val method = invocation.method
            val where = "${method.declaringClass.simpleName}.${method.name}"
            val tenantId = TenantContext.currentTenantId()
            if (!TenantIdentifierResolver.isTenant(tenantId)) {
                throw IllegalStateException("Tenant data needs a tenant: $where")
            }
            sessionTenantIds().forEach { sessionTenantId ->
                if (sessionTenantId != tenantId) {
                    throw IllegalStateException("The transaction was started for another tenant: $where")
                }
            }
            return invocation.proceed()
        }

        private fun sessionTenantIds(): List<Any?> = TransactionSynchronizationManager.getResourceMap().values
            .filterIsInstance<EntityManagerHolder>()
            .map { it.entityManager.unwrap(Session::class.java).getTenantIdentifierValue() }
    }
}

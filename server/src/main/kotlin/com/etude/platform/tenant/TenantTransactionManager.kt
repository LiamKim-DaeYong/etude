package com.etude.platform.tenant

import jakarta.persistence.EntityManagerFactory
import org.hibernate.Session
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.CannotCreateTransactionException
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.UUID

/**
 * 트랜잭션을 시작할 때 데이터베이스에 현재 회사를 알린다(ADR-0006 2.3).
 *
 * 값은 Hibernate 세션이 쓰는 테넌트를 그대로 쓴다. 두 방어선(`@TenantId`, RLS)이 서로 다른 회사를 보지 않는다.
 * `set_config(..., true)`는 트랜잭션이 끝나면 사라지므로, 커넥션 풀이 연결을 다음 요청에 넘겨도 값이 남지 않는다.
 * 회사가 미지정이거나 플랫폼 블록이면 빈 값으로 두어 RLS가 어떤 테넌트 행도 보여 주지 않는다.
 */
class TenantTransactionManager(entityManagerFactory: EntityManagerFactory) : JpaTransactionManager(entityManagerFactory) {

    override fun doBegin(transaction: Any, definition: TransactionDefinition) {
        super.doBegin(transaction, definition)
        val holder = TransactionSynchronizationManager.getResource(obtainEntityManagerFactory()) as EntityManagerHolder
        try {
            val session = holder.entityManager.unwrap(Session::class.java)
            val tenantId = session.getTenantIdentifierValue() as UUID?
            val setting = tenantId?.takeIf(TenantIdentifierResolver::isTenant)?.toString() ?: ""
            session.doWork { connection ->
                connection.prepareStatement(SET_TENANT).use {
                    it.setString(1, setting)
                    it.execute()
                }
            }
        } catch (ex: RuntimeException) {
            // 회사를 알리지 못한 트랜잭션은 쓰지 않는다. 시작한 트랜잭션을 되돌리고 자원을 정리한다.
            holder.entityManager.transaction.rollback()
            doCleanupAfterCompletion(transaction)
            throw CannotCreateTransactionException("Could not set the current tenant on the transaction", ex)
        }
    }

    private companion object {
        const val SET_TENANT = "select set_config('app.tenant_id', ?, true)"
    }
}

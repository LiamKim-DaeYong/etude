package com.etude.platform.tenant

import com.etude.shared.TenantScopedEntity
import org.hibernate.Interceptor
import org.hibernate.type.Type

/**
 * 실제 회사가 정해지지 않은 작업(미지정, 플랫폼 블록)에서 테넌트 데이터를 바꾸지 못하게 한다(ADR-0006 2.6, ADR-0008).
 *
 * Hibernate는 root 테넌트로 저장하면 오류 없이 root 값이나 지정한 회사로 저장하고, 미지정이면 미지정 값으로 저장한다.
 * 그래서 [TenantScopedEntity]를 저장·수정·삭제하기 전에 예외를 던진다. 데이터베이스에서는 RLS가 같은 쓰기를 거절한다.
 */
object PlatformWriteGuard : Interceptor {

    override fun onPersist(entity: Any, id: Any?, state: Array<out Any?>, propertyNames: Array<out String>, types: Array<out Type>): Boolean {
        check(entity, "save")
        return false
    }

    override fun onFlushDirty(
        entity: Any,
        id: Any?,
        currentState: Array<out Any?>,
        previousState: Array<out Any?>?,
        propertyNames: Array<out String>,
        types: Array<out Type>,
    ): Boolean {
        check(entity, "update")
        return false
    }

    override fun onRemove(entity: Any, id: Any?, state: Array<out Any?>, propertyNames: Array<out String>, types: Array<out Type>) {
        check(entity, "delete")
    }

    private fun check(entity: Any, action: String) {
        if (entity is TenantScopedEntity && !TenantIdentifierResolver.isTenantBound()) {
            throw IllegalStateException("Cannot $action tenant data without a tenant: ${entity.javaClass.simpleName}")
        }
    }
}

package com.etude.board.application

import com.etude.platform.TenantContext
import com.etude.tenant.TenantRegistered
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 새 회사의 기본 게시판을 준비한다(테넌트 초기화, ADR-0008).
 *
 * 회사 등록이 커밋된 뒤 비동기로 실행되고, 처리 결과는 이벤트 발행 기록에 남는다.
 * 이벤트의 회사로 전환한 뒤에 트랜잭션을 시작해야 Hibernate와 RLS가 그 회사로 동작한다.
 * 그래서 리스너 자체가 트랜잭션인 `@ApplicationModuleListener`는 쓰지 않는다.
 * 다시 전달되어도 준비는 멱등이다.
 */
@Component
class TenantRegisteredListener(
    private val boardService: BoardService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    @TransactionalEventListener
    fun on(event: TenantRegistered) {
        TenantContext.runAs(event.tenantId) {
            try {
                boardService.prepareDefaultBoard()
            } catch (ex: DataIntegrityViolationException) {
                // 동시에 다른 쪽이 먼저 만들었다면 그 게시판을 쓴다.
                boardService.findDefaultBoard() ?: throw ex
                log.debug("Default board already prepared for tenant {}", event.tenantId)
            }
        }
    }
}

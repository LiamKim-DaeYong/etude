package com.etude.inventory.domain

import com.etude.platform.LOCK_TIMEOUT_MILLIS
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.Repository
import java.util.UUID

interface ReservationRepository : Repository<Reservation, UUID> {

    fun save(reservation: Reservation): Reservation

    /**
     * 주문의 예약 기록을 잠그고 조회한다. 중복 전달된 이벤트는 잠금을 기다린 뒤 해제할 기록을 찾지 못한다.
     * [LOCK_TIMEOUT_MILLIS] 안에 잠그지 못하면 실패한다(ADR-0003).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MILLIS))
    @Query("select r from Reservation r where r.orderId = :orderId and r.status = :status order by r.productId")
    fun findByOrderIdAndStatusForUpdate(orderId: UUID, status: ReservationStatus): List<Reservation>
}

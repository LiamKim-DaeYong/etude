# ADR-0003: 재고 예약의 동시성 제어에 비관적 락 사용

| 항목 | 내용 |
| --- | --- |
| 상태 | 채택 |
| 결정일 | 2026-11-08 |
| 적용 범위 | 재고 예약(`InventoryService.reserve`) |
| 구현 상태 | 재고 예약에 적용 |
| 검증 상태 | 초과 판매 재현·해결, 락 대기 시간 초과를 통합 테스트로 검증. 방식별 처리량·지연 비교는 측정하지 않음 |

## 요약

- **결정:** 재고 줄을 비관적 락(`PESSIMISTIC_WRITE`, PostgreSQL `FOR NO KEY UPDATE`)으로 읽고 예약한다. 락은 2초까지만 기다리고, 넘기면 503 `SERVICE_UNAVAILABLE`과 `Retry-After`로 응답한다.
- **이유:** 인기 상품에는 주문이 몰려 충돌이 흔하다. 예약은 주문 트랜잭션 안에서 일어나므로 어떤 방식이든 락은 주문 커밋까지 유지된다. 그렇다면 업무 규칙을 엔티티에 둔 채 정확성을 얻는 방식이 낫다.
- **비용:** 같은 상품의 예약이 한 번에 하나씩 처리된다. 락 대기 시간 힌트 때문에 잠그는 조회마다 SQL이 3개 더 나간다.
- **재검토:** SELECT 왕복이 병목으로 측정되거나 예약이 주문 트랜잭션 밖으로 나가면 조건부 UPDATE를, 한 상품에 요청이 몰려 락 대기 자체가 병목이 되면 Redis를 검토한다.

## 1. 배경

주문할 때 `OrderService.place`는 한 트랜잭션에서 상품 확인 → 금액 확인 → 재고 예약 → 주문 저장을 한다(#84). 예약은 재고 줄을 락 없이 읽고, 메모리에서 수량을 뺀 뒤, 커밋할 때 계산한 값을 쓴다.

```sql
select ... from inventory_item where product_id = ?
update inventory_item set available_quantity = ?, updated_at = ? where id = ?   -- 계산한 값
```

두 트랜잭션이 같은 수량을 읽으면 나중에 쓴 쪽이 먼저 쓴 쪽의 변경을 덮어쓴다(갱신 손실). PostgreSQL은 두 번째 UPDATE를 첫 번째가 커밋할 때까지 기다리게 하지만, 기다린 쪽이 쓰는 값은 이미 옛 수량으로 계산한 것이다. `available_quantity >= 0` 체크 제약도 막지 못한다. 각 트랜잭션은 음수가 아닌 값을 쓰기 때문이다.

재고 10개인 상품에 1개씩 예약 100건을 동시에 넣으면(아래 환경) 88건, 94건, 96건이 성공했다. 정답은 10건이다.

고려한 조건은 다음과 같다.

- **트랜잭션 경계:** 예약은 주문 트랜잭션 안에서 일어난다. 락을 잡는 방식이라면 락은 주문이 커밋될 때까지 유지된다.
- **충돌 빈도:** 인기 상품 하나에 주문이 몰리는 것이 쇼핑몰의 기본 상황이다. 충돌이 드물다고 가정할 수 없다.
- **전부 아니면 전무:** 한 주문의 여러 상품을 모두 예약하거나 하나도 예약하지 않는다.
- **여러 서버:** 서버 인스턴스가 늘어도 동작해야 한다.

## 2. 결정

1. 예약할 재고 줄은 `findByProductIdForUpdate`(`@Lock(PESSIMISTIC_WRITE)`)로 읽는다. Hibernate 7.4는 PostgreSQL에 `FOR NO KEY UPDATE`를 보낸다. 같은 줄을 고치려는 트랜잭션끼리는 서로 막고, 이 줄을 외래 키로 참조하는 INSERT는 막지 않는다. 잠글 필요가 없는 조회(재고 등록, 재고 조회)는 기존 `findByProductId`를 쓴다.
2. 락은 2초까지만 기다린다. JPA 힌트 `jakarta.persistence.lock.timeout`을 쓰고, Hibernate는 이를 조회 앞뒤의 `set local lock_timeout`으로 바꾼다.
3. 락을 얻지 못하면 Spring이 `PessimisticLockingFailureException`(`CannotAcquireLockException`)으로 바꿔 던진다. `GlobalExceptionHandler`는 이를 503 `SERVICE_UNAVAILABLE`과 `Retry-After: 1`로 응답한다. 재고 부족(409 `INSUFFICIENT_INVENTORY`)은 "다시 해도 안 된다", 락 대기 시간 초과는 "잠시 뒤 다시 하면 될 수 있다"는 뜻이므로 구분한다. 락 대기 시간 초과는 재고만의 업무 규칙이 아니라 어느 모듈에서나 생길 수 있는 상황이라 공통 처리기에 둔다.
4. 여러 상품은 상품 ID 순으로 잠근다(#84에서 도입). 이후 재고를 복원하는 기능(주문 취소 등)도 같은 순서를 지킨다.
5. 주문 트랜잭션 안에서 느린 일을 하지 않는다. 특히 외부 결제 호출을 이 트랜잭션에 넣으면 결제를 기다리는 동안 재고 줄이 잠긴다. 결제 설계(9~10주)는 이를 전제로 한다.

## 3. 대안과 선택 이유

| 대안 | 장점 | 판단 |
| --- | --- | --- |
| 낙관적 락 (`@Version`) | 락을 잡지 않는다. 충돌이 드물면 가장 가볍다 | 충돌이 흔한 조건에서는 재고가 남아 있어도 대부분 버전 충돌로 실패한다. 재시도하려면 롤백된 주문 트랜잭션 전체(`place`)를 다시 해야 한다. 관리자의 재고 수정이나 주문 상태 변경처럼 충돌이 드문 곳에 쓴다. |
| 조건부 UPDATE (`set available_quantity = available_quantity - ? where ... and available_quantity >= ?`) | 읽기 없이 한 문장으로 끝난다. 잠그는 조회보다 SQL이 적다 | 유효한 대안이다. UPDATE도 줄 락을 커밋까지 유지하므로 이 구조에서는 락 유지 시간이 비관적 락과 거의 같고, 대기 시간 제한도 따로 걸어야 한다. 대신 "예약 수량은 판매 가능 수량을 넘지 않는다"는 규칙이 엔티티에서 SQL로 옮겨 가고, 부족할 때의 판매 가능 수량을 다시 조회해야 하며, 영속성 컨텍스트와 감사(`updated_at`)를 우회한다. 지금은 그 대가가 더 크다고 판단했다. |
| 애플리케이션 락 (`synchronized`, `ReentrantLock`) | DB 부하가 없다 | 한 JVM 안에서만 동작한다. 서버가 두 대가 되면 깨진다. |
| Redis (원자 연산, Lua, 분산 락) | DB 밖에서 처리해 DB 줄 락 대기를 없앤다 | 저장소가 하나 늘고, DB와의 정합성을 따로 맞춰야 한다. 한정 판매 수준의 트래픽에서 다룬다(11~12주). |

락 대기 시간을 거는 위치도 비교했다.

| 방법 | 추가 SQL | 영향 범위 | 판단 |
| --- | --- | --- | --- |
| JPA 쿼리 힌트 | 잠그는 조회마다 3개 (`current_setting` 읽기, 설정, 되돌리기) | 그 조회 하나 | **선택.** 실행해 동작을 확인했고, 락 제한이 걸린 곳이 코드에 보인다. |
| 예약 시작 시 `SET LOCAL lock_timeout` | 트랜잭션마다 1개 | 그 트랜잭션 전체 | 추가 SQL 비용이 문제로 측정되면 바꾼다. |
| 커넥션 설정 (Hikari `connection-init-sql`) | 없음 | 모든 SQL (Flyway 마이그레이션 포함) | 범위가 너무 넓다. |

JPA 힌트는 실행 계획을 강제하는 옵티마이저 힌트(Oracle `/*+ INDEX */` 등)와 다르다. 구현체에 옵션을 넘길 뿐이고 실행 계획은 바뀌지 않는다. 다만 구현체가 지원하지 않는 힌트는 오류 없이 무시된다. 그래서 락 대기 시간 초과를 통합 테스트로 확인해 힌트가 무시되면 테스트가 실패하게 한다.

## 4. 트레이드오프

- 같은 상품의 예약은 한 번에 하나씩 처리된다. 정확성을 얻는 대신 그 상품에 대한 동시 처리를 포기한다. 다른 상품끼리는 서로 막지 않는다.
- 락은 주문 커밋까지 유지되므로, 예약 뒤의 주문 저장 시간도 락 유지 시간에 들어간다.
- 잠그는 조회마다 SQL이 3개 더 나간다. 상품 한 줄을 예약하는 데 SQL이 5개(조건부 UPDATE는 1개) 든다. 같은 네트워크 안의 왕복이지만 비용은 측정하지 않았다.
- 503은 클라이언트에게 재시도를 맡긴다. 재시도가 몰리면 부하가 더 커질 수 있다. `Retry-After` 값과 재시도 정책은 부하 측정 때 다시 본다.

## 5. 검증 결과와 한계

환경: Windows 11, Docker(Testcontainers) PostgreSQL 18.6, Spring Boot 4.1.2, Hibernate 7.4.13, HikariCP 기본 커넥션 풀 10개. 재고 10개, 1개씩 예약 100건, 스레드 32개가 동시에 출발한다(`InventoryReservationConcurrencyTest`).

| 조건 | 성공 | 재고 부족 | 남은 재고 |
| --- | --- | --- | --- |
| 락 없음 (3회) | 88, 94, 96 | 12, 6, 4 | 1, —, 3 |
| 비관적 락 (3회 + 전체 빌드) | 10 | 90 | 0 |

락 없음 조건의 두 번째 실행(94건)은 남은 재고를 기록하지 않았다.

- 다른 트랜잭션이 재고 줄을 잡고 있으면 예약은 약 2초(2,033ms) 뒤 `CannotAcquireLockException`으로 실패하고, 재고는 바뀌지 않는다.
- 상품 2개를 예약할 때 Hibernate가 상품마다 `current_setting` 읽기, `set local lock_timeout = 2000`, 잠그는 조회, `set local lock_timeout = 0`을 보내는 것을 SQL 로그로 확인했다.

검증하지 않은 것:

- 방식별 처리량과 지연 시간. 위 결과는 정확성 검증이며 성능 비교가 아니다.
- 데드락. 상품 ID 순 잠금으로 막는다고 판단했지만 테스트로 재현하지 않았다. 데드락 예외가 503으로 바뀌는지도 확인하지 않았다.
- 2초와 `Retry-After: 1`은 측정 근거가 없는 값이다.
- 서버 여러 대, 실제 네트워크 지연, 한정 판매 수준의 트래픽.

## 6. 재검토 조건

- **조건부 UPDATE:** 잠그는 조회의 왕복이 병목으로 측정되거나, 예약이 주문 트랜잭션에서 빠져나와 짧은 트랜잭션으로 돌게 될 때.
- **Redis:** 한 상품에 요청이 몰려 줄 락 대기가 병목이 될 때. 이때도 DB에는 판매 가능 수량이 음수가 되지 않게 하는 마지막 장치가 필요하며, 조건부 UPDATE가 그 자리에 맞는다.
- **락 대기 시간·재시도:** 부하 측정에서 503 비율이나 재시도 부하가 문제가 될 때.

## 참고 자료

- [PostgreSQL: Explicit Locking — Row-Level Locks](https://www.postgresql.org/docs/18/explicit-locking.html#LOCKING-ROWS)
- [PostgreSQL: `lock_timeout`](https://www.postgresql.org/docs/18/runtime-config-client.html#GUC-LOCK-TIMEOUT)
- [PostgreSQL: Transaction Isolation — Read Committed](https://www.postgresql.org/docs/18/transaction-iso.html#XACT-READ-COMMITTED)
- [Jakarta Persistence 3.2 명세 (`jakarta.persistence.lock.timeout`)](https://jakarta.ee/specifications/persistence/3.2/)
- [Spring Data JPA: Locking](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html)

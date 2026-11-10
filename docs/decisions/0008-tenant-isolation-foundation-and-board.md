# ADR-0008: 회사 미지정 작업의 경계와 첫 테넌트 데이터(게시판)

| 항목 | 내용 |
| --- | --- |
| 상태 | 채택 |
| 결정일 | 2026-11-11 |
| 관련 결정 | [ADR-0006](0006-tenant-data-isolation.md)의 2.6 "테넌트가 정해지지 않으면 실패한다"를 이루는 **수단**을 바꾼다(원칙은 유지). [ADR-0005](0005-product-direction.md) 개발 순서에 게시판을 더하고, [ADR-0007](0007-module-layers.md) 층에 `board` 모듈을 둔다 |
| 적용 범위 | 회사(테넌트) 지정, 애플리케이션 DB 역할, 테넌트 데이터의 읽기·쓰기 경계, 게시판·게시글, 기본 게시판 준비 |
| 구현 상태 | 구현(#121). 테넌트 데이터는 게시판·게시글뿐이다. 상품·주문·재고의 테넌트 전환은 구현 전 |
| 검증 상태 | 통합 테스트(Testcontainers)와 로컬 실행으로 확인(6절). 성능과 운영 역할 구성은 확인하지 않았다 |

## 요약

- **회사 미지정은 "어떤 회사도 아님"이다.** 회사가 정해지지 않은 작업은 Hibernate에 미지정 값 `NONE`을 알린다. `NONE`은 root(플랫폼 블록)도 실제 회사도 아니다. 세션은 열리지만 `@TenantId` 조건이 어떤 행에도 맞지 않고, 데이터베이스의 `app.tenant_id`는 빈 값이라 RLS가 모든 테넌트 행을 막는다.
- **실패는 공통 경계에서 낸다.** 테넌트 데이터(`TenantScopedEntity`)의 저장소는 실제 회사가 정해진 작업에서만 동작한다. 미지정·플랫폼이거나, 이미 열린 트랜잭션이 다른 회사로 시작됐으면 조회·저장 전에 예외를 던진다. 저장·수정·삭제는 Hibernate 가드와 RLS가 한 번 더 막는다.
- **두 겹 방어 유지:** Hibernate `@TenantId`와 RLS, 소유자가 아닌 애플리케이션 역할(`etude_app`), 트랜잭션마다 `set_config('app.tenant_id', ?, true)`.
- **게시판:** `board` 모듈(서비스 공통 층)에 게시판(`Board`)과 게시글(`Post`)을 나눠 둔다. 첫 버전은 회사마다 기본 게시판 하나다.
- **기본 게시판 준비:** 조회 요청은 아무것도 만들지 않는다. 회사 등록 이벤트(`TenantRegistered`)를 받아 그 회사로 멱등하게 준비한다(테넌트 초기화).

## 1. 배경

ADR-0006 4절은 "현재 테넌트 없이 세션 사용 → `HibernateException`"을 확인했고, 2.6은 이를 근거로 "Hibernate는 테넌트 없이 세션을 열지 않는다"고 적었다. 게시판(#121)이 첫 `@TenantId` 엔티티가 되자 이 동작이 애플리케이션 전체에 걸렸다.

측정(#121, Hibernate 7.4.13, Spring Boot 4.1.2, Testcontainers PostgreSQL 18.6):

| 상황 | 결과 |
| --- | --- |
| `@TenantId` 엔티티 하나 추가, 테넌트 미지정 시 resolver가 `null` | **애플리케이션이 시작되지 않는다.** Spring Data가 시작할 때 파생 쿼리(`TenantRepository.findByCode` 등)를 만들며 테넌트 없이 세션을 열고, Hibernate가 `SessionFactory configured for multi-tenancy, but no tenant identifier specified`로 거절한다. `EtudeApplicationTests`와 인증·상품·주문·재고 컨트롤러 테스트가 모두 실패했다 |
| 원인 | Hibernate는 `@TenantId` 필터가 하나라도 정의되면 세션마다 테넌트 값을 요구한다(`AbstractSharedSessionContract.setUpMultitenancy`). 테넌트 데이터가 아닌 엔티티만 쓰는 세션도 같다 |

시작 문제를 풀어도 같은 경로로 로그인 없는 고객 API(`/api/v1`), 비동기 이벤트 리스너, 이벤트 발행 기록 처리가 실패한다고 추론했다(시작이 먼저 실패해 따로 측정하지 못했다). 회사로 나누지 않은 모듈이 남아 있는 동안 "세션 자체를 거절"은 서버 시작·익명 API·비동기 처리와 함께 갈 수 없다.

## 2. 결정

### 2.1 회사 미지정 값 `NONE`

- `CurrentTenantIdentifierResolver`는 `TenantContext`가 비어 있으면 `NONE`(`00000000-0000-0000-0000-000000000001`)을 돌려준다. 회원가입으로 발급하는 UUIDv7과 겹치지 않는다.
- `NONE`은 root가 아니다(`isRoot`는 플랫폼 블록만 참). 테넌트 필터가 `NONE`으로 걸려 테넌트 행은 0건이다.
- 트랜잭션은 `NONE`·플랫폼이면 `app.tenant_id`를 빈 값으로 둔다. RLS가 읽기 0건, 쓰기 거절이다.
- `NONE`은 `platform.tenant` 안에만 있고 모듈 공개 API가 아니다. 누가 `TenantContext.runAs(NONE)`로 넣어도 실제 회사로 보지 않는다.
- 플랫폼 권한으로 자동 승격하지 않는다. 고객 API나 익명 요청을 플랫폼 블록으로 실행하지 않는다.

### 2.2 공통 경계: 테넌트 데이터 저장소

`platform`이 `TenantScopedEntity`를 다루는 모든 Spring Data 저장소에 자동으로 검사를 붙인다(`TenantRepositoryGuard`). 저장소의 트랜잭션 처리 안쪽에서 실행되고, 다음이면 조회·저장 전에 `IllegalStateException`을 던진다(응답은 500 `INTERNAL_ERROR`).

1. 현재 회사가 실제 회사가 아니다(미지정, `NONE`, 플랫폼 블록).
2. 이미 열린 트랜잭션의 Hibernate 세션이 지금과 다른 회사로 열렸다. Spring은 트랜잭션 안에서 바뀐 `TenantContext`를 보지 않고 기존 세션을 쓰므로, 그대로 두면 이전 회사의 행을 돌려준다. Hibernate의 `validateExistingCurrentSessions`는 `getCurrentSession()`에만 적용되어 이 경우를 막지 않는다.

이후 모듈은 엔티티가 `TenantScopedEntity`를 상속하는 것만으로 같은 규칙을 받는다. 서비스마다 따로 확인하지 않는다.

### 2.3 쓰기 가드

- **Hibernate:** `Interceptor`(`PlatformWriteGuard`)가 실제 회사가 아닌 작업에서 `TenantScopedEntity`를 저장·수정·삭제하면 예외를 던진다. 저장소를 거치지 않는 `EntityManager` 사용도 막는다. `shared`는 `platform`을 모르므로 가드는 `platform`에 두고 Hibernate 설정으로 건다(ADR-0006 2.6은 `TenantScopedEntity`가 던진다고 적었다).
- **데이터베이스:** RLS `WITH CHECK`가 현재 회사가 아닌 행의 삽입·수정을 거절한다.

### 2.4 데이터베이스 역할

| 역할 | 접속하는 곳 | 권한 |
| --- | --- | --- |
| 소유자(로컬 `etude`, 테스트는 컨테이너 사용자) | Flyway | 테이블을 만들고 소유한다. RLS를 받지 않는다 |
| `etude_app` | 애플리케이션 | `LOGIN`, `NOSUPERUSER`, `NOBYPASSRLS`. 모듈 마이그레이션이 자기 테이블에 필요한 권한만 준다 |

- 역할 생성은 인프라가 한다: 로컬은 `server/db/init`을 `compose.yaml`이 처음 데이터베이스를 만들 때 실행하고, 테스트는 같은 스크립트를 Testcontainers가 실행한다. 운영은 같은 이름의 역할을 만들고 비밀번호를 비밀 저장소로 관리한다.
- 권한은 각 모듈의 마이그레이션이 준다(`grant ... to etude_app`). 회사로 나누기 전 테이블은 지금 동작대로 `select, insert, update, delete`, 게시판·게시글은 삭제 기록을 남기므로 `delete`를 주지 않는다.
- Spring Modulith의 이벤트 발행 기록 테이블 자동 생성은 끈다. Flyway(`__root`)가 만들고, 애플리케이션 역할은 테이블을 만들 수 없다.

### 2.5 트랜잭션별 회사 지정

`JpaTransactionManager`를 확장한 `TenantTransactionManager`가 트랜잭션을 시작한 직후, 그 트랜잭션의 Hibernate 세션이 쓰는 테넌트 값으로 `set_config('app.tenant_id', ?, true)`를 실행한다. 두 방어선이 같은 값을 본다(ADR-0006 2.5). 값은 트랜잭션이 끝나면 사라진다. 설정에 실패하면 트랜잭션을 되돌리고 시작하지 않는다.

### 2.6 게시판 모듈

- **층:** 서비스 공통(ADR-0007 2.1). 여러 서비스의 사용자가 함께 쓰는 회사 안의 소통 기능이다. `allowedDependencies = ["tenant", "identity", "shared", "platform"]`. `tenant`는 회사 등록 이벤트, `identity`는 작성자 아이디 조회(`UserDirectory`)에 쓴다.
- **모델:** 게시판(`board`)과 게시글(`post`)을 나눈다. 게시판을 늘려도 글과 API 경로(`/admin/v1/boards/{boardId}/posts`)가 그대로다. 게시글은 `(board_id, tenant_id)` 복합 외래 키로 같은 회사의 게시판만 가리킨다.
- **권한(첫 버전):** 같은 회사의 로그인 사용자는 읽고 쓴다. 수정·삭제는 작성자만 하고 아니면 403 `NOT_POST_AUTHOR`. 대표의 다른 사람 글 관리, 게시판별 작성 제한은 없다.
- **삭제:** `deleted_at`을 남기고 목록·상세에서 뺀다.
- **식별자:** 다른 회사의 게시판·글 ID, 다른 게시판의 글 ID는 없는 것과 같게 404 `NOT_FOUND`다. 존재 여부를 드러내지 않는다.
- **작성자 표시:** 표시 이름이 없으므로 `authorUsername`(아이디)을 쓴다. 글에 복사하지 않고 조회한다.
- **개발 순서:** ADR-0005의 순서에 없던 게시판을 상품 테넌트 전환보다 먼저 한다(#118, #119). 상품·주문·재고의 전환은 따로 한다.

### 2.7 기본 게시판 준비

- 조회 요청(`GET`)은 게시판을 만들지 않는다. 기본 게시판이 없으면 빈 목록이다.
- `tenant` 모듈이 회사를 등록한 트랜잭션에서 `TenantRegistered(tenantId)`를 발행한다. `board` 모듈의 리스너가 커밋 뒤 비동기로 받아 `TenantContext.runAs(tenantId)` 안에서 새 트랜잭션으로 기본 게시판을 준비한다. 처리 결과는 이벤트 발행 기록에 남고 다시 전달할 수 있다.
- 준비는 멱등이다. 있으면 그대로 두고, 동시에 준비하면 부분 유일 인덱스 `(tenant_id) where is_default`가 하나만 남긴다. 늦은 쪽은 유일 인덱스 위반을 받고 이미 있는 게시판을 쓴다.
- ADR-0006 5절의 "이벤트 리스너의 테넌트"는 이 이벤트에 한해 **이벤트에 회사 ID를 담고 리스너가 그 회사로 전환**하는 방식으로 정한다. 일반 규칙은 테넌트 데이터를 다루는 리스너가 더 생길 때 정한다.

## 3. 대안

| 대안 | 판단 |
| --- | --- |
| **A. 미지정 = `NONE`, 공통 경계에서 실패** | **선택.** 서버 시작, 로그인, 고객 API, 비동기 처리의 기존 동작이 그대로이고, 테넌트 데이터는 공통 경계·Hibernate 가드·RLS가 막는다 |
| B. 고객 API 전체를 플랫폼 블록으로 실행하고, 시작·리스너에도 우회를 둔다 | 비권장·미승인. root가 넓어지고 방문자 식별(ADR-0006 2.5) 전에 고객 API에 플랫폼 권한을 준다 |
| C. 게시판을 `@TenantId` 없이 RLS와 직접 쓴 조건으로만 격리 | 비권장. 두 겹 방어가 한 겹이 된다 |
| 기본 게시판을 첫 조회에서 만들기 | 철회(#120). 조회가 쓰기를 한다 |
| 회원가입 트랜잭션에서 기본 게시판 저장 | 불가. 회원가입은 플랫폼 블록이라 쓰기 가드가 막고, `identity`가 `board`를 알게 된다 |
| Flyway로 기존 회사에 일괄 삽입 | 비권장. 모듈별 마이그레이션 순서가 보장되지 않고 `tenant` 테이블과 결합된다 |

## 4. 트레이드오프

- **실패 시점이 늦어졌다:** 회사를 빠뜨린 코드는 세션을 열 때가 아니라 테넌트 데이터 저장소를 부를 때 실패한다. 저장소를 거치지 않고 `EntityManager`로 테넌트 데이터를 **읽으면** 공통 경계를 지나지 않는다. 이때는 Hibernate 필터(`NONE`)와 RLS가 0건을 돌려준다(조용한 빈 결과). 테넌트 데이터는 저장소로만 다룬다.
- **트랜잭션 안에서 회사를 바꿀 수 없다:** 다른 회사의 일을 하려면 새 트랜잭션을 연다.
- **역할이 둘이다:** 로컬·테스트·운영에 소유자와 애플리케이션 역할을 모두 둬야 한다. 기존 로컬 데이터베이스에는 `etude_app`이 없으므로 다시 만들거나 역할을 직접 만든다.
- **기본 게시판은 회사 등록 직후 잠깐 없을 수 있다:** 비동기라 등록 직후 조회가 빈 목록일 수 있다. 리스너가 실패하면 발행 기록이 미완료로 남고, 재전달해야 게시판이 생긴다.
- **재시작 후 미완료 이벤트 자동 재전달은 켜지 않았다.**

## 5. 남은 일

- 상품·주문·재고의 테넌트 전환(#105), 방문자의 회사 식별(ADR-0006 2.5).
- 테넌트 데이터를 다루는 리스너의 일반 규칙, 미완료 이벤트 재전달 운영 방식.
- 운영 환경의 역할 생성과 비밀번호 관리.

## 6. 검증

환경: Windows 11, Docker 29.1.3, Testcontainers PostgreSQL 18.6, Hibernate ORM 7.4.13, Spring Boot 4.1.2.

**측정(통합 테스트, `./gradlew build` 통과):**

| 확인한 것 | 테스트 |
| --- | --- |
| 서버 시작, 인증, 기존 고객 API, 기존 비동기 리스너가 테스트 수정 없이 통과 | 기존 104개(`EtudeApplicationTests`, `AuthControllerTest`, `ProductControllerTest`, `OrderControllerTest`, `OrderCanceledListenerTest` 등) |
| 애플리케이션 역할이 슈퍼유저가 아니고 `BYPASSRLS`가 없으며 `board`·`post`의 소유자가 아니다. 두 테이블에 RLS가 켜져 있다 | `TenantIsolationTest` |
| 회사 설정 없이는 0건, 회사 A로는 A 행만 보인다 | `TenantIsolationTest` |
| 다른 회사의 행이나 회사 없이 삽입하면 RLS가 거절하고, 다른 회사 행 수정은 0건이다 | `TenantIsolationTest` |
| 미지정·플랫폼·`runAs(NONE)`에서 테넌트 데이터 저장소가 예외를 던지고, 회사로 나누지 않은 테이블은 그대로 쓴다 | `TenantIsolationTest` |
| 미지정·플랫폼에서 `EntityManager.persist`로 저장하면 Hibernate 가드가 거절한다 | `TenantIsolationTest` |
| 열린 트랜잭션 안에서 회사를 바꾸면 거절한다(회사 A→B, 미지정→B) | `TenantIsolationTest` |
| 트랜잭션의 `app.tenant_id`가 Hibernate 회사와 같고, 같은 풀 연결(`pg_backend_pid` 동일)의 다음 사용에서는 비어 있으며 행이 보이지 않는다. 그 연결로 회사 B 트랜잭션을 열면 B만 보인다 | `TenantIsolationTest` |
| 회사 등록 후 기본 게시판이 하나 생긴다. 조회는 만들지 않는다. 다시 준비, 동시 준비 8건, 이벤트 재전달에도 하나다 | `DefaultBoardPreparationTest` |
| 작성→조회→수정→삭제, 같은 회사 직원 읽기·쓰기, 작성자 아닌 수정·삭제 403(대표 포함), 다른 회사 404, 다른 게시판 글 ID 404, 비로그인 401, CSRF 없음 403, 입력·페이지 검증 400, 최신순 페이지 | `PostAdminControllerTest` |
| 모든 응답이 커밋한 OpenAPI 문서와 맞는다 | `OpenApiContract`(모든 통합 테스트) |

**로컬 실행:** `compose.yaml`로 만든 새 데이터베이스(별도 compose 프로젝트 이름)에 `bootRun`으로 실제 HTTP 요청을 보냈다(#121 PR에 결과).

**확인하지 않은 것:**

- `set_config`와 RLS 정책의 성능 비용(측정 안 함).
- 운영 환경의 역할 구성.
- 비동기 리스너 실패 뒤 재시작 시 자동 재전달.

## 7. 재검토 조건

- 상품·주문·재고를 회사로 나누고 방문자 식별이 생겨 회사 미지정 작업이 서버 시작과 플랫폼 블록만 남으면, 미지정 세션을 다시 거절하는 방식을 검토한다.
- 테넌트 데이터를 저장소 밖에서 읽는 코드가 필요해지면 그 경로에도 같은 경계를 둔다.

## 참고 자료

- [Hibernate ORM 7 javadoc: `CurrentTenantIdentifierResolver`](https://docs.hibernate.org/orm/7.0/javadocs/org/hibernate/context/spi/CurrentTenantIdentifierResolver.html)
- [Spring Data Commons: `RepositoryFactoryCustomizer`](https://docs.spring.io/spring-data/commons/reference/)
- [PostgreSQL: Row Security Policies](https://www.postgresql.org/docs/18/ddl-rowsecurity.html)
- [ADR-0006: 테넌트 데이터 격리와 식별](0006-tenant-data-isolation.md)

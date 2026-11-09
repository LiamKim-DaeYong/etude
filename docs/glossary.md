# 용어집

Etude의 업무 용어와 이름 규칙의 기준 문서다. 코드, API, DB, 문서, 이슈와 PR은 이 문서의 용어를 쓴다.

- **한 개념에는 한 단어:** 같은 개념을 다른 단어로 부르지 않는다. "쓰지 않는 말"에 적은 동의어와 오해를 부르는 단어는 쓰지 않는다.
- **모듈마다 뜻이 다를 수 있다:** 같은 단어라도 모듈(경계)에 따라 가리키는 대상이 다르면 모듈 절에 따로 정의한다. 예: 주문의 상품명은 주문 시점에 복사한 값이다.
- **용어 변경은 설계 변경이다:** 용어를 추가하거나 바꾸는 PR은 같은 PR에서 이 문서를 고친다.
- **영어 이름:** 미국 영어, 업계에서 흔히 쓰는 단어를 고른다. 영어로 읽었을 때 다른 뜻이 되는 단어는 피한다.

## 이름 규칙

### 동사

| 동사 | 쓰는 곳 | 뜻 | 쓰지 않는 말 |
| --- | --- | --- | --- |
| `register` | 서비스 | 관리자가 상품·재고 같은 기준 데이터를 처음 만든다. 대표가 직원을 등록할 때도 쓴다 | `create`, `add` |
| `signUp` | 서비스 | 회원가입. 회사와 대표를 함께 만든다 | `join`, `register` (회원가입에) |
| `find` | 서비스, 모듈 공개 API | 조회한다. 없으면 `null`을 돌려준다 (`findOrder`) | 없을 수 있는 조회에 `get` |
| `get` | 컨트롤러 핸들러 | 조회한다. 반드시 돌려주고, 없으면 예외를 던진다(컨트롤러는 404). 핸들러 이름은 `operationId`가 된다 (`getOrder`, Google AIP-131의 Get) | `null`을 돌려주는 조회에 `get` |
| `place` | 주문 | 고객이 주문한다 | `create`, `submit` |
| `reserve` | 재고 | 주문에 쓸 수량을 판매 가능 수량에서 떼어 둔다 | `deduct`, `decrease`, `allocate`, `commit` |
| `cancel` | 주문 | 고객이 주문을 취소한다 | `abort`, `void`, `revoke` |
| `release` | 재고 | 예약한 수량을 판매 가능 수량으로 되돌린다 | `restore`, `rollback`, `unreserve`, `increase` |

### 접미사

| 접미사 | 뜻 | 예 |
| --- | --- | --- |
| `Info` | 모듈 공개 API가 다른 모듈에 돌려주는 값 (모듈 기본 패키지) | `ProductInfo` |
| `Details` | 애플리케이션 서비스가 웹 계층에 돌려주는 값 (트랜잭션 안에서 만든다) | `OrderDetails` |
| `Command` | 애플리케이션 서비스의 입력 | `PlaceOrderCommand` |
| `Request`, `Response` | 웹 요청·응답 DTO. JSON 형식을 책임진다 | `PlaceOrderRequest` |
| `RejectedException` | 업무 규칙에 따른 거절. 사유는 중첩 enum `Reason` | `OrderRejectedException` |
| `Service`, `Repository` | 애플리케이션 서비스, 저장소 | `OrderService` |
| `Controller`, `AdminController` | 고객 API, 관리자 API 컨트롤러 | `ProductAdminController` |

### 값과 필드

| 대상 | 규칙 | 예 |
| --- | --- | --- |
| 식별자 | 자기 자신은 `id`, 다른 엔티티는 `<엔티티>Id` | `customerId`, `productId` |
| 상태 | 필드는 `status`, 타입은 `<엔티티>Status`. 값은 형용사나 과거분사 | `OrderStatus.PLACED` |
| 시각 | `<과거분사>At`, `Instant`(UTC) | `placedAt`, `createdAt` |
| 가능 여부 | `<동사>able`, 엔티티의 `Boolean` 속성. 서비스는 이 값으로 먼저 거절하고, 엔티티는 같은 조건을 다시 지킨다 | `orderable`, `cancelable` |
| 이벤트 | `<엔티티><과거분사>`, 발행한 모듈의 기본 패키지에 둔다. 다른 모듈이 조회할 수 있는 식별자만 담는다 | `OrderCanceled` |
| 금액 | `Money` 하나로 둔다. 이름에 `amount`, `price`를 덧붙이지 않는다 | `total` (`totalAmount` 아님) |
| 수량 | `Int`, 무엇의 수량인지 모호하면 앞에 붙인다 | `quantity`, `availableQuantity` |
| 줄임말 | 쓰지 않는다. `id`만 예외 | `lineNumber` (`lineNo` 아님) |
| 오류 코드 | `UPPER_SNAKE_CASE`. 이 문서의 용어로 상황을 쓴다 | `PRODUCT_NOT_FOUND`, `TOTAL_MISMATCH` |
| DB | 테이블은 단수형 `snake_case`. 예약어인 `orders`, `users`만 복수형 | `order_line`, `inventory_item` |

## 공통

| 한글 | 코드 | API | 정의 | 쓰지 않는 말 |
| --- | --- | --- | --- | --- |
| 테넌트 | `Tenant` | — | 서비스에 회원가입한 회사 하나. 각 테넌트는 자기 데이터만 본다 ([ADR-0005](decisions/0005-product-direction.md), [ADR-0006](decisions/0006-tenant-data-isolation.md)) | 고객사, 업체, `company`, `organization` (테넌트를 가리킬 때) |
| 금액 | `Money` | `{"amount", "currency"}` | 금액과 ISO 4217 통화 코드의 쌍 | 금액·통화를 따로 둔 필드 |
| 통화 | `currency` | `currency` | ISO 4217 코드 (`KRW`, `USD`) | `currencyCode` |
| 고객 API | — | `/api/v1` | 고객이 쓰는 API | — |
| 관리자 API | `*AdminController` | `/admin/v1` | 운영자가 쓰는 백오피스 API | `backoffice`, `internal` |
| 거절 | `*RejectedException`, `Reason` | `code` | 요청은 올바르지만 업무 규칙상 처리할 수 없음 | `fail`, `error` (업무 거절에) |

| 공통 오류 코드 | 정의 |
| --- | --- |
| `INVALID_REQUEST` | 요청 형식이나 값이 잘못됐다 (400). 필드별 오류는 `errors`에 담는다 |
| `NOT_FOUND` | 요청한 리소스가 없다 (404) |
| `INTERNAL_ERROR` | 예상하지 못한 서버 오류 (500). 상세 내용은 숨긴다 |
| `AUTHENTICATION_REQUIRED` | 로그인이 필요한 API를 로그인하지 않고(또는 세션이 끝난 뒤) 불렀다 (401) |
| `INVALID_CREDENTIALS` | 로그인 정보가 맞지 않는다 (401). 없는 사용자와 틀린 비밀번호를 구분하지 않는다 |
| `ACCESS_DENIED` | 로그인했지만 이 사용자에게 허용되지 않은 요청이다 (403) |
| `CSRF_TOKEN_INVALID` | 상태를 바꾸는 요청에 CSRF 토큰이 없거나 맞지 않다 (403) |

## 테넌트 (`tenant`)

서비스에 가입한 회사를 다룬다([ADR-0006](decisions/0006-tenant-data-isolation.md)).

| 한글 | 코드 | API | 정의 | 쓰지 않는 말 |
| --- | --- | --- | --- | --- |
| 회사 | `Tenant` | — | 회원가입으로 생기는 테넌트. 창고와 공장을 운영한다 | `Company` (거래처도 회사라 혼동) |
| 회사 코드 | `code` | — | 직원이 로그인할 때 입력하는, 시스템이 발급한 고정 번호. 테넌트 ID와 다르다 | `alias` (홈페이지 별칭과 구분) |

## 사용자 (`identity`)

회사에 속해 로그인하는 사람과 그 사람의 역할을 다룬다([ADR-0007](decisions/0007-module-layers.md)).

| 한글 | 코드 | API | 정의 | 쓰지 않는 말 |
| --- | --- | --- | --- | --- |
| 사용자 | `User` | — | 회사에 속해 로그인하는 사람. 회사 하나에만 속한다 | `Account`, 계정 (회계의 계정과목, 결제의 계좌와 혼동), `Member` (쇼핑몰 회원과 혼동) |
| 역할 | `UserRole`, `role` | — | 사용자가 회사 안에서 할 수 있는 일의 범위 | — |
| 대표 | `UserRole.OWNER` | — | 회원가입한 사용자. 이메일로 로그인하고, 직원을 등록하고 비밀번호를 재설정한다 | `ADMIN`, `MASTER` |
| 직원 | `UserRole.STAFF` | — | 대표가 등록한 사용자. 회사 코드와 아이디로 로그인한다 | `EMPLOYEE` (인사·생산의 작업자와 구분) |
| 아이디 | `username` | — | 회사 안에서 유일한 로그인 이름 | `loginId`, `userId` (식별자 규칙과 혼동) |
| 회원가입 | `signUp` | — | 회사와 대표를 함께 만든다 | `join`, `register` |
| 로그인 | `login` | `POST /admin/v1/auth/owner-login`, `POST /admin/v1/auth/staff-login` | 대표는 이메일, 직원은 회사 코드와 아이디로 세션을 연다 | `signIn` (로그인 API·코드에서) |
| 로그아웃 | `logout` | `POST /admin/v1/auth/logout` | 세션을 끝낸다 | `signOut` |
| 로그인한 사용자 | `SignedInUser` | `GET /admin/v1/auth/session` | 세션에 담긴 사용자. 소속 회사로 요청마다 테넌트를 정한다 | — |

생산 현장의 작업자처럼 로그인하지 않고 기록에만 남는 사람은 사용자가 아니다. 해당 모듈에서 따로 정한다.

## 카탈로그 (`catalog`)

판매할 상품을 정의한다. "무엇을 파는가"를 다룬다.

| 한글 | 코드 | API | 정의 | 쓰지 않는 말 |
| --- | --- | --- | --- | --- |
| 상품 | `Product` | `/api/v1/products`, `/admin/v1/products` | 판매하는 물건 | `item`, `goods` |
| 상품명 | `name` | `name` | 상품의 현재 이름 | `title` |
| 가격 | `price` | `price` | 상품의 현재 판매 가격 | `amount`, `cost` |
| 상품 상태 | `ProductStatus`, `status` | `status` | 판매 여부 | `salesStatus`, `SalesStatus` |
| 판매 중 | `ProductStatus.ACTIVE` | `ACTIVE` | 주문할 수 있는 상태 | `ON_SALE` (할인 중으로 읽힘) |
| 판매 중지 | `ProductStatus.SUSPENDED` | `SUSPENDED` | 운영자가 판매를 멈춘 상태 | `INACTIVE`, `STOPPED` |
| 주문 가능 | `orderable` | — | 상품이 판매 중이라 주문할 수 있음. 재고와 무관하다 | `available`, `purchasable` |

## 재고 (`inventory`)

상품별로 몇 개를 팔 수 있는지 다룬다. 재고를 가리키는 영어 단어는 `inventory` 하나만 쓴다.

| 한글 | 코드 | API | 정의 | 쓰지 않는 말 |
| --- | --- | --- | --- | --- |
| 재고 | `InventoryItem` | `/admin/v1/inventory-items/{productId}` | 상품 하나의 재고 기록. 상품마다 하나다 | `stock`, `Stock`, `stocks` (주식으로 읽힘) |
| 판매 가능 수량 | `availableQuantity` | `availableQuantity` | 지금 주문할 수 있는 수량 | `quantity` (단독), `stockQuantity` |
| 요청 수량 | `requestedQuantity` | — | 예약하려는 수량. 재고 부족을 알릴 때 판매 가능 수량과 함께 쓴다 | `requested` (단독) |
| 재고 등록 | `register` | `POST /admin/v1/inventory-items` | 상품의 재고 기록을 처음 만든다 | `create` |
| 재고 예약 | `reserve` | — | 주문에 쓸 수량을 판매 가능 수량에서 떼어 둔다. 주문할 때 예약하고, 주문을 취소하면 해제한다 | `deduct`, `decrease`, 예약 판매(pre-order)의 "예약" |
| 예약 기록 | `Reservation` | — | 주문 하나가 상품 하나를 얼마나 예약했는지의 기록. 주문·상품마다 하나다 | `Allocation`, `Hold` |
| 예약 상태 | `ReservationStatus`, `status` | — | 예약 기록이 아직 수량을 잡고 있는지 | — |
| 예약됨 | `ReservationStatus.RESERVED` | — | 수량을 떼어 둔 상태 | `ACTIVE`, `HELD` |
| 해제됨 | `ReservationStatus.RELEASED` | — | 수량을 판매 가능 수량으로 되돌린 상태. 다시 해제하지 않는다 | `CANCELED` (주문 상태와 혼동) |
| 예약 해제 | `release` | — | 예약됨 상태의 예약 기록만큼 판매 가능 수량을 되돌린다. 같은 주문으로 다시 해제해도 바뀌지 않는다 | `restore`, `rollback`, `unreserve` |
| 재고 부족 | `InsufficientInventoryException` | `INSUFFICIENT_INVENTORY` | 요청 수량이 판매 가능 수량보다 많다 | `OUT_OF_STOCK`, `INSUFFICIENT_STOCK` |

| 거절 코드 | 정의 |
| --- | --- |
| `PRODUCT_NOT_FOUND` | 재고를 등록할 상품이 없다 |
| `INVENTORY_ITEM_ALREADY_REGISTERED` | 상품의 재고가 이미 등록되어 있다 |

예약 포트 `InventoryReservation`, `ReservationLine`과 재고 부족 예외 `InsufficientInventoryException`은 쓰는 쪽인 주문 모듈(`com.etude.order`)이 정의하고 재고 모듈이 구현한다([ADR-0004](decisions/0004-module-dependency-direction.md)).

재고 상태(실제 보유, 예약, 판매 가능)를 나누거나 예약을 확정하는 용어는 결제 단계(#20)에서 정한다. 참고로 업계에서는 주문 시점의 동작을 reserve(Magento, commercetools)나 commit(Shopify)이라 하고, deduct는 출고할 때 실제 재고를 빼는 동작(Magento source deduction)을 가리킨다.

## 주문 (`order`)

고객의 주문과 주문 항목을 다룬다.

| 한글 | 코드 | API | 정의 | 쓰지 않는 말 |
| --- | --- | --- | --- | --- |
| 주문 | `Order` | `/api/v1/orders` | 고객이 상품을 사겠다고 확정한 요청과 그 기록 | `purchase` |
| 주문하다 | `place` | `POST /api/v1/orders` | 주문을 만든다 | `create`, `submit` |
| 고객 | `customerId` | `customerId` | 주문한 사람 | `user`, `member`, `buyer` |
| 주문 항목 | `OrderLine` | `lines[]` | 주문 안의 상품 한 줄. 상품마다 한 항목 | `OrderItem`, `LineItem`, 주문 라인 |
| 항목 번호 | `lineNumber` | `lineNumber` | 주문 안에서 항목의 순번 (1부터) | `lineNo`, `seq` |
| 주문 상태 | `OrderStatus` | `status` | 주문의 진행 단계 | — |
| 주문됨 | `OrderStatus.PLACED`, `OrderLineStatus.PLACED` | `PLACED` | 주문이 접수된 상태 | `ORDERED`, `CREATED` |
| 주문 시각 | `placedAt` | `placedAt` | 고객이 주문한 시각 (업무 시각). 감사용 `createdAt`과 구분한다 | `orderedAt`, `orderDate` |
| 주문 취소 | `cancel` | `POST /api/v1/orders/{id}/cancel` | 주문을 취소한다. 주문 항목도 함께 취소되고, 예약한 재고는 해제된다 | `abort`, `void`, `DELETE /orders/{id}` |
| 취소됨 | `OrderStatus.CANCELED`, `OrderLineStatus.CANCELED` | `CANCELED` | 주문이 취소된 상태 | `CANCELLED` (영국식 철자), `ABORTED` |
| 취소 가능 | `cancelable` | — | 주문됨 상태라 취소할 수 있음 | `cancellable` |
| 취소 시각 | `canceledAt` | `canceledAt` | 주문을 취소한 시각. 취소하지 않은 주문은 `null` | `cancelledAt`, `cancelDate` |
| 주문 취소됨 | `OrderCanceled` | — | 주문을 취소한 트랜잭션에서 발행하는 이벤트. 주문 ID만 담는다 | `OrderCancelEvent`, `OrderCancelled` |
| 상품명 | `productName` | `productName` | 주문 시점에 복사한 상품명. 이후 상품명이 바뀌어도 그대로다 | — |
| 단가 | `unitPrice` | `unitPrice` | 주문 시점에 확정한 상품 하나의 가격 | `price` (주문 항목에서) |
| 항목 금액 | `lineTotal` | `lineTotal` | 단가 × 수량 | `subtotal`, `lineAmount` |
| 총액 | `total` | `total` | 항목 금액의 합 | `totalAmount`, `totalPrice` |
| 예상 총액 | `expectedTotal` | `expectedTotal` | 고객이 주문 전에 확인한 총액. 서버가 계산한 총액과 비교한다 | — |
| 현재 총액 | `currentTotal` | `currentTotal` | 총액이 다를 때 거절 응답에 담는 서버 계산 총액 | — |

| 거절 코드 | 정의 |
| --- | --- |
| `PRODUCT_NOT_FOUND` | 주문한 상품이 없다 |
| `PRODUCT_NOT_ORDERABLE` | 상품이 판매 중이 아니다 |
| `CURRENCY_MISMATCH` | 주문 항목들의 통화가 다르다 |
| `TOTAL_MISMATCH` | 예상 총액과 현재 총액이 다르다 (`TOTAL_AMOUNT_MISMATCH` 아님) |
| `INSUFFICIENT_INVENTORY` | 주문 수량만큼 재고가 없다. 재고가 등록되지 않은 상품도 같다. 부족한 상품은 응답의 `productId`에 담는다 (`OUT_OF_STOCK` 아님) |

| 취소 거절 코드 | 정의 |
| --- | --- |
| `ORDER_NOT_CANCELABLE` | 주문됨 상태가 아니라 취소할 수 없다 (409). 이미 취소한 주문도 같다 |

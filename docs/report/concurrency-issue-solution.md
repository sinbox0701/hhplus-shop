## 1. 문제 식별

동시성이 높은 환경에서 다음 세 가지 핵심 비즈니스 로직이 올바르게 동작하지 않을 위험이 존재한다.

- **상품 옵션의 재고 차감 및 복원**
  - 주문이 몰릴 경우 재고가 마이너스로 빠져들어 "오버셀(over-sell)" 발생
- **유저의 포인트 잔액**
  - 동시 다발적인 포인트 사용·충전 요청 시 잔액 불일치
- **선착순 쿠폰 발급**
  - 제한된 수량의 쿠폰을 '첫 번째' 요청자에게만 정확히 지급하지 못함

---

## 2. 분석

각 항목별로 동시성 경쟁도 및 충돌 허용 수준을 검토하였다.

| 항목        | 경쟁 강도 | 충돌 허용 여부 | 재시도 로직 허용 여부         |
| ----------- | --------- | -------------- | ----------------------------- |
| 상품 재고   | 높음      | 불허           | 불허 (UX 상 실패 허용 어려움) |
| 포인트 잔액 | 낮음~중간 | 허용 가능      | 허용 (재시도 로직 구현 용이)  |
| 선착순 쿠폰 | 매우 높음 | 불허           | 불허 ('첫 호출' 보장 필요)    |

---

## 3. 해결

### 3.1 상품 옵션 재고 차감 및 복원

- **락 전략**: PESSIMISTIC_WRITE
- **구현 요약**
  1. `JpaProductOptionRepository`에 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 적용
  2. `findByIdWithPessimisticLock()`를 통해 **행 단위 잠금(SELECT … FOR UPDATE)**
  3. `ProductOptionRepositoryImpl`에서 `updateWithPessimisticLock()` 메소드를 통해 비관적 락 적용
  4. 재고 부족 시 예외 발생, 정상 차감 후 트랜잭션 커밋

```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT o FROM ProductOptionEntity o WHERE o.id = :id")
fun findByIdWithPessimisticLock(@Param("id") id: Long): ProductOptionEntity?
```

### 3.2 유저의 포인트 잔액 관리

- **락 전략**: Optimistic Locking (낙관적 락)
- **구현 요약**
  1. `AccountEntity`에 `@Version` 필드 추가
  2. `Account` 도메인 모델에 version 필드 포함
  3. `AccountRepository`에서 낙관적 락을 활용한 업데이트 메소드 제공
  4. 충돌(`OptimisticLockingFailureException`) 발생 시 애플리케이션에서 재시도 로직 수행

```kotlin
@Entity
@Table(name = "accounts")
class AccountEntity(
    // ... 다른 필드들 ...

    @Version
    val version: Long = 0
)
```

### 3.3 선착순 쿠폰 발급

- **락 전략**: PESSIMISTIC_WRITE
- **구현 요약**
  1. `JpaCouponRepository`에 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 적용
  2. `findByCodeWithPessimisticLock()`을 통해 쿠폰 행 잠금
  3. `CouponRepositoryImpl`에서 트랜잭션 내에서 비관적 락 활용
  4. 쿠폰 수량 소진 시 예외 발생, 정상 발급 시 수량 차감

```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM CouponEntity c WHERE c.code = :code")
fun findByCodeWithPessimisticLock(@Param("code") code: String): CouponEntity?
```

## 4. 대안

### 상품 재고

- Redis 분산락(SET NX PX)을 사용하여 DB 접근 전 애플리케이션 레벨에서 잠금
- Kafka / RabbitMQ 기반 주문 큐로 직렬화 처리

### 포인트 잔액

- 비관적 락(PESSIMISTIC_WRITE) 적용 후 트랜잭션 짧게 유지
- CQRS 패턴으로 쓰기 전용 서비스 분리

### 선착순 쿠폰

- UPDATE coupon SET available_count = available_count - 1 WHERE code = ? AND available_count > 0 쿼리로 원자성 보장
- Redis LPUSH / BRPOP 큐로 선착순 로직 구현

각 대안은 시스템 아키텍처, 장애 허용 수준, 운영 복잡도 등을 고려하여 선택할 수 있다.

---

## 5. 권위 계층(authoritative layer) 정리

여러 락 전략을 무분별하게 중첩하면(예: Redisson 분산 락 + 비관적 락 + `@Version`을
한 메서드에 동시에) 어느 계층이 정합성을 책임지는지 모호해지고, 특정 계층을 제거해도
테스트가 통과해 "가짜 안전감"을 준다. 따라서 각 흐름마다 **권위 계층을 하나로 확정**한다.

| 흐름 | 권위 계층 | 최종 방어선 | 비고 |
| --- | --- | --- | --- |
| 사용자별 쿠폰 발급 (`CouponFacade.create`) | Redisson 분산 락 (`coupon:user:{userId}`) | `user_coupon` (userId, couponId) 유니크 제약 | 다중 인스턴스 직렬화가 목적이므로 단일 노드 DB 락을 중첩하지 않음 |
| 선착순 한정 수량 차감 (`CouponService.issueFirstComeFirstServedCoupon`) | DB 비관적 락 (`PESSIMISTIC_WRITE`) | 쿠폰 재고 row 잠금 | 공유 재고 row를 다투므로 DB 락이 권위. Redisson 중첩하지 않음 |
| 상품 옵션 재고 차감/복원 | DB 비관적 락 (`PESSIMISTIC_WRITE`) | 재고 row 잠금 | |
| 계좌 잔액 충전/차감 | 낙관적 락 (`@Version`) | 충돌 시 재시도 | 경쟁 강도가 낮고 재시도가 용이 |

원칙: **"인스턴스 간 사용자 단위 직렬화 = Redisson", "단일 공유 row 정합성 = DB 락",
"낮은 경쟁 + 재시도 허용 = 낙관적 락"** 으로 역할을 분리한다. 코드 상 권위 계층은
`CouponFacade`의 KDoc에 명시되어 있으며, 이 문서와 일치시킨다.
